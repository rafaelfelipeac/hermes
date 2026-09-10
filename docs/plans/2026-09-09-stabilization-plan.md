# Plano de estabilização e refatoração — próxima versão

**Status: em implementação. P01 concluído localmente em 09/09/2026. P02 implementado localmente em 09/09/2026; execução instrumentada real pendente de emulador/CI.**

**Branch:** `feat/refactor`. **Base de referência:** `030513a`.

Este plano cobre os 11 achados da auditoria de 09/09/2026, dentro das etapas 1 e 2 aprovadas como direção. O trabalho será integrado na mesma branch para uma única próxima versão. Os blocos abaixo representam unidades de implementação e revisão, não releases ou períodos de espera.

## 1. Objetivo e limite de escopo

Entregar operações de dados consistentes, backup confiável, idioma funcional nas versões Android suportadas, restauração previsível de estado, atualização correta de data e verificações automáticas que protejam as refatorações.

Entram agora as correções de execução na main thread já identificadas e a separação de cálculo de desafios do editor. Ficam adiados profiling de histórico, cold start, benchmarks, caches, paginação, novos índices por performance, limites arbitrários para arquivos, telemetria, reestruturação em módulos e revisão ampla do design system. Não será necessário usar o backup pessoal do Rafael.

Também ficam fora desta implementação publicação na Play Store, criação de tags, merge e escolha antecipada de um novo número de versão. O resultado será preparado para revisão e posterior publicação.

## 2. Decisões propostas para aprovação junto do plano

| Tema | Proposta |
|---|---|
| Banco e backup | Manter Room v7 e JSON v6, com importação dos schemas 1–6. As correções previstas não precisam mudar a estrutura persistida. |
| Contratos internos | Adicionar comandos/repositórios específicos para operações compostas; atualizar todos os chamadores e fakes na mesma mudança. Não expor Room à UI ou ao domínio. |
| Activity | Incluir dados e ação correspondente na mesma transação Room nos comandos migrados. Se gravar a ação falhar, o comando inteiro falha; a UI não anuncia sucesso. |
| Erros de mutação | Conservar o conteúdo editado quando a escrita falhar e apresentar recuperação. Nenhum lançamento assíncrono deve anunciar conclusão antes da persistência. |
| Undo | Preservar duração e comportamento atuais durante a sessão. Não tornar undo durável após morte do processo. Restaurar undo deve usar as mesmas garantias transacionais dos comandos. |
| Backup em andamento | Sobreviver à rotação via ViewModel. Após morte do processo, não repetir automaticamente importação/exportação; restaurar apenas uma confirmação pendente recuperável ou informar que a operação foi interrompida. |
| Confirmação de importação | Guardar o documento selecionado em arquivo temporário privado e salvar apenas sua referência/etapa. Reconfirmar antes de substituir dados após restauração; limpar o temporário ao terminar/cancelar. |
| Preferências importadas | Aplicar as preferências do backup em uma única edição DataStore. Preservar dados Room já importados se essa etapa falhar e informar resultado parcial. Não prometer transação entre Room e DataStore. |
| Idioma | Preferência por integrar `AppCompatActivity` e tema compatível, com DataStore como fonte persistida existente. Validar inicialização e `StringProvider` antes de fechar a implementação. |
| Pacote de idiomas | Se o risco de language splits se confirmar, preferir incluir as traduções no AAB a adicionar infraestrutura de download. A escolha favorece simplicidade e uso offline. |
| Navegação | Preservar destinos, hierarquia e comportamento de voltar. Modificar somente propriedade/restauração de seleção e rascunhos nos fluxos afetados. |
| CI | Usar o GitHub Actions existente: testes locais/análise e job instrumentado de persistência em um emulador. Matriz adicional de idioma/restauração focada nas versões necessárias. |

Os nomes de novas classes abaixo são propostas de responsabilidade, não contratos definitivos. Pequenos ajustes de nomes/arquivos poderão seguir os padrões encontrados durante implementação.

Este plano torna explícitas as alterações de contratos internos e restauração para revisão conforme `AGENTS.md`. Uma necessidade nova de schema, API externa ou mudança de navegação além do descrito exige revisão antes de implementar.

## 3. Rastreabilidade dos achados

| Achado | Blocos que o resolvem | Evidência de conclusão |
|---|---|---|
| 1 — Falso sucesso na exportação | P06, P07 | Falha/cancelamento não atualizam timestamp nem produzem sucesso |
| 2 — Gravações parciais | P03, P04, P05, P09 | Rollback de dados e Activity sob falhas intermediárias |
| 3 — Idioma Android ≤32 | P08 | Texto/formatadores corretos antes e depois de reabrir, API antiga/moderna |
| 4 — Restauração inconsistente | P07, P10 | Confirmação, editor e seleção recuperáveis sem repetir mutações |
| 5 — Exportação inconsistente | P06 | Snapshot relacional consistente sob escrita concorrente |
| 6 — Trabalho na main/recomputação | P06, P11 | Processamento fora da main; edição não recalcula histórico |
| 7 — Comandos de eventos duplicados | P05, P09 | Mesma operação produz dados/metadados equivalentes pelas duas telas |
| 8 — Dia/semana desatualizados | P11 | Mudança de dia/retomada atualizam projeções sem escrita no banco |
| 9 — Gates quebrados | P01 | Detekt e Lint sem erros |
| 10 — KtLint cobre apenas scripts | P01 | Kotlin de produção e testes realmente incluído |
| 11 — Proteção insuficiente de migrações | P02, P12 | Migração de fixtures com dados e instrumentados executados pela CI |

## 4. Sequência e dependências

Ordem preferida: **P01 → P02 → P03 → P04 → P05 → P06 → P07 → P08 → P09 → P10 → P11 → P12**.

- P01 estabelece feedback confiável antes de alterar comportamento.
- P02 fornece proteção com Room real para P03–P07 e P09.
- P05 estabelece os comandos transacionais; P09 migra os dois consumidores e remove duplicações.
- P06 estabelece o contrato seguro de backup; P07 cuida da operação e restauração da tela.
- P08 valida a interação entre troca de idioma e a operação de importação de P07.
- P10 pode ser executado antes de P09 se houver conveniência, mas não depende dele.
- P11 separa projeções de UI depois de proteger os comandos.
- P12 fecha regressão e documentação do conjunto.

Execução serial por padrão, sem novas branches ou tarefas paralelas. Cada bloco pode gerar mais de um commit quando regras, integração e componentes puderem ser revisados separadamente. Nunca deixar testes deliberadamente falhando ao concluir um bloco.

## 5. Blocos de implementação

### P01 — Restabelecer os quality gates

**Status:** concluído localmente em 09/09/2026. **Achados:** 9/10. **Dependências:** nenhuma. **Esforço:** pequeno a médio. **Risco:** baixo.

**Arquivos existentes:** `app/build.gradle.kts`, `gradle/libs.versions.toml` se necessário para compatibilidade comprovada; `ActivityUiFormatterTest.kt`, `DemoWorkoutFixtures.kt`, `PersonalRecordEntryEditorDialog.kt`, `TrophiesScreenSections.kt`.

**Passos:**

1. Reexecutar as verificações na base atual, pois outras correções podem ter entrado desde a auditoria.
2. Corrigir os dois problemas de imports e usar o locale observável já adotado no projeto para a data de troféus.
3. Dividir `ActivityUiFormatterTest` por famílias de comportamento, preservando assertions e cenários. Extrair apenas fixtures compartilhadas úteis.
4. Identificar por que o plugin KtLint registra apenas scripts na combinação atual de plugins. Preferir corrigir descoberta/configuração; usar tarefa de fontes explícita se for a alternativa menor e confiável.
5. Se surgirem novos erros de estilo, corrigi-los em uma mudança mecânica separada. Não aplicar formatação indiscriminada ao repositório.

**Testes/verificação:** testes do formatter e locale; Detekt, Lint e KtLint completos. Conferir lista de fontes. Em cópia descartável, introduzir erro em `.kt` de produção e teste e confirmar falha do KtLint, sem modificar a árvore de trabalho principal.

**Aceite:** gates verdes; `.kt` de `main`, `test` e `androidTest` incluídos. Avisos existentes classificados; não é requisito zerar avisos de atualização/plurais sem análise.

### P02 — Cobrir migrações com dados e habilitar instrumentados

**Status:** implementado localmente em 09/09/2026; compilação/gates passaram, sem dispositivo local conectado para executar instrumentados. **Achado:** 11. **Dependências:** P01. **Esforço:** médio. **Risco:** baixo no produto.

**Arquivos:** `.github/workflows/build-and-lint.yml`, `.github/workflows/release.yml` apenas para reaproveitar verificações se adequado; `ChallengesMigrationTest.kt`, `PersonalRecordsMigrationTest.kt`; novos testes de migração antiga em `androidTest/core/database`. Usar os schemas 1–7 existentes em `app/schemas`.

**Passos:**

1. Criar fixtures compatíveis com cada schema de origem; não inserir campos que só surgiram depois.
2. Cobrir caminho 1→7 e caminhos com dados de recordes/desafios das versões 4/5/6 até 7.
3. Verificar valores, IDs, completion, conversão de descanso/eventType, referências e integridade, além da estrutura validada pelo Room.
4. Adicionar job de emulador Linux com versão de API disponível no runner, começando por API 32 para o conjunto de persistência. Fixar versão/imagem em configuração, sem depender de dispositivo pessoal.
5. Executar em PR e permitir execução manual na branch; incluir falhas e relatórios como artifacts mesmo quando a tarefa falhar.
6. Evitar repetir os mesmos testes locais em etapas redundantes de build; manter responsabilidades das tarefas explícitas.

**Testes/verificação:** executar migrações, backup e repositórios instrumentados. Usar banco temporário por teste e limpar estado de preferências quando necessário.

**Aceite:** fixtures antigas chegam à v7 com conteúdo preservado; CI realmente executa a seleção de classes. Configuração do workflow, sozinha, não conta como execução aprovada. Se não houver infraestrutura local/remota disponível, registrar a validação como pendente.

### P03 — Comandos atômicos de categorias

**Achado:** 2. **Dependências:** P02. **Esforço/risco:** médios.

**Arquivos:** `CategoriesViewModel.kt`, repositórios/DAOs de categorias, treinos, recordes e desafios, `CategoryModule.kt`, logger Room e testes de categorias. Novo coordenador em `features/categories/data` e contrato de comando no domínio da feature.

**Passos:**

1. Caracterizar metadata de exclusão e reordenação com as assertions existentes.
2. Mover a reassociação das três features e exclusão da categoria para uma única transação Room.
3. Ler a categoria atual dentro do comando; preservar proteção de `UNCATEGORIZED_ID` e os destinos existentes: treinos sem categoria e referências nulas em recordes/desafios.
4. Fazer troca de ordem com leitura e duas escritas na mesma transação, sem depender de posições antigas capturadas da UI.
5. Gravar a ação por `UserActionLogger` no contexto da mesma transação. Não duplicar logging no ViewModel.
6. Retornar resultado da operação para a UI; registro removido ou repetição sem alteração não deve gerar evento de sucesso fictício.

**Testes:** falha na reassociação de recordes/desafios ou no logger → categoria, vínculos, ordem e histórico anteriores. Sucesso → todas as relações esperadas e uma ação. Duas exclusões sequenciais → apenas a primeira modifica/loga. Reordenações rápidas usam estado atual do banco.

**Aceite:** nenhuma operação concluída parcialmente; ViewModel apenas despacha e apresenta resultado. Nenhuma mudança de schema.

**Status:** implementado localmente em 10/09/2026. Compilação, unit tests, Detekt e ktlint passaram; execução instrumentada real de `RoomCategoryCommandRepositoryTest` permanece pendente porque não há emulador/dispositivo conectado.

### P04 — Excluir entrada manual de recorde com integridade

**Achado:** 2. **Dependências:** P02 e padrão de P03. **Esforço:** pequeno a médio. **Risco:** médio.

**Arquivos:** `PersonalRecordsViewModel.kt`, `PersonalRecordsRepositoryImpl.kt`, `PersonalRecordDao.kt`, `PersonalRecordsModule.kt`, testes de ViewModel/repositório.

**Passos:** ler entrada/família atuais; remover entrada e limpar referência manual na mesma transação; atualizar somente os campos necessários da família, evitando sobrescrever outros campos com uma cópia antiga; registrar a ação na transação; remover a coordenação do ViewModel.

**Testes:** excluir entrada atual manual; excluir outra entrada; entrada inexistente; falhar na limpeza da referência ou no logger; exportar/importar após exclusão bem-sucedida. Em todos os casos, conferir preservação de título, categoria, unidade e demais entradas.

**Aceite:** nenhuma referência manual órfã produzida pelo comando e metadados existentes preservados.

**Status:** implementado localmente em 10/09/2026. Compilação, unit tests, Detekt e ktlint passaram; execução instrumentada real de `RoomPersonalRecordCommandRepositoryTest` permanece pendente porque não há emulador/dispositivo conectado.

### P05 — Estabelecer comandos transacionais de treino/evento

**Status parcial:** conclusão, exclusão, movimentação/reordenação e edição de detalhes de treino/evento implementadas localmente em 10/09/2026 com comandos transacionais e idempotentes. Compilação, unit tests, Detekt e ktlint passaram; execução instrumentada real de `RoomWeeklyTrainingCommandRepositoryTest` permanece pendente porque não há emulador/dispositivo conectado. Undo e cópia de semana ainda ficam para os próximos commits do P05.

**Achados:** 2/7. **Dependências:** P02–P04. **Esforço:** maior bloco. **Risco:** médio a alto.

**Arquivos:** `WeeklyTrainingRepository.kt`, `WeeklyTrainingRepositoryImpl.kt`, `WorkoutDao.kt`, `WorkoutOrdering.kt`, `WeekDateUtils.kt`, `WeeklyTrainingViewModelHelpers.kt`, `WorkoutChangeDependencies.kt`, wiring Hilt e testes semanais. Novos modelos de pedido/resultado em arquivos próprios e coordenador de mutações em `features/weeklytraining/data`.

**Passos:**

1. Registrar invariantes: semana canônica, semana exibida, bucket de itens sem data, posição por dia/slot, datas de eventos, completion e identidade usada pelo undo.
2. Extrair regras de ordenação/data ainda embutidas em comandos, com testes puros antes de trocar os chamadores.
3. Receber intenção de movimento/edição, reler registros atuais e calcular alterações dentro da transação. Não aceitar um snapshot de UI como fonte final de verdade.
4. Tornar atômicos mudança de agenda, detalhes e normalização de origem/destino, junto da ação correspondente.
5. Definir conclusão idempotente: estado desejado igual ao persistido não grava nem loga novamente. Usar a transação como fronteira de coordenação entre ViewModels, não apenas mutex local.
6. Integrar undo e verificar cópia de semana: preservar IDs/completion/regras atuais e eliminar caminhos de escrita em lote sem atomicidade nos consumidores migrados.
7. Considerar resultado de erro sem consumir undo ou fechar editor como sucesso. Preservar cancelamento antes do commit; nunca engolir `CancellationException` em um `Result` genérico.

**Testes:** mover entre slots/dias/semanas e para sem data; semana começando em dia diferente de segunda; ordem com lacunas; falha na segunda alteração e no logger; concluir duas vezes com emissão atrasada; completar o último item uma única vez; undo de movimento/conclusão/exclusão; cópia preservando as regras de reset de completion.

**Aceite:** falhas deixam dados/Activity anteriores; comandos repetidos usam estado persistido; snapshots de undo correspondem ao estado realmente alterado. Dividir este bloco em commits de regras, comandos e integração semanal.

### P06 — Tornar o repositório de backup consistente e seguro para a main

**Achados:** 1/5/6. **Dependências:** P02. **Esforço/risco:** médios.

**Arquivos:** `BackupRepositoryImpl.kt`, `BackupSnapshotExporter.kt`, `BackupDatabaseWriter.kt`, `BackupSnapshotValidator.kt`, `SettingsRepository.kt`, `SettingsRepositoryImpl.kt`, `ImportBackupResult.kt`, DI e testes de backup/settings.

**Passos:**

1. Colocar consultas das tabelas em uma transação curta de leitura. Capturar um snapshot de preferências em uma leitura DataStore; explicitar que Room e DataStore não formam uma transação única.
2. Serializar fora da transação e fora da main; decodificar/validar também em contexto de CPU. Injetar dispatchers onde isso for necessário para teste determinístico.
3. Fazer o contrato de falha de exportação incluir erros da construção do snapshot, hoje fora do `runCatching` de encoding; preservar cancelamento.
4. Aplicar preferências importadas numa única edição DataStore, depois do commit Room.
5. Representar falha de preferências como sucesso parcial de dados. Adaptar resultado, mensagem e logging sem reclassificar dados importados como rollback. Reutilizar chaves de metadata existentes quando expressarem corretamente o resultado.
6. Manter schemas e decoders; atualizar `docs/backup-compatibility-policy.md` para documentar garantias e resultado parcial, sem modificar decoders antigos desnecessariamente.

**Testes:** snapshot concorrente de família/entrada e desafio/progresso; exceção de leitura retorna falha; schema futuro/inválido não escreve; erro no meio do replace reverte; falha DataStore mantém Room importado e preferências anteriores; cancelamento não vira sucesso; round-trip schemas 1–6 continua válido.

**Aceite:** exportação consistente dentro do Room, sem trabalho de JSON na main e sem sucesso total quando apenas parte da importação concluiu. Não há promessa de recuperação durável da operação após morte do processo.

### P07 — Extrair e estabilizar a operação de backup na UI

**Achados:** 1/4 e refatoração de Browse. **Dependências:** P06. **Esforço/risco:** médios.

**Arquivos:** `BrowseScreen.kt`, `SettingsViewModel.kt`, `SettingsBackupScreen.kt`, `SettingsState.kt`. Novos `BackupViewModel`, estado de operação e gateway Android de documentos em `features/backup`; wiring Hilt e testes correspondentes.

**Estado proposto:** ocioso → seleção/leitura → confirmação, quando necessária → execução → resultado. Exportação pode passar de tentativa em pasta para espera do “Salvar como”. Um único comando de backup ativo por vez. O seletor continua na UI; IO fica no gateway; coordenação fica no ViewModel.

**Passos:**

1. Extrair acesso ao `ContentResolver` e `DocumentFile` para gateway testável com implementação Android e fake.
2. Corrigir resultado da pasta: JSON gerado não significa arquivo gravado. Falha abre fallback quando aplicável; cancelamento preserva timestamp; sucesso só após fechar a escrita com êxito.
3. Associar tentativa/resultado ao documento correto; callbacks antigos ou repetidos não disparam operação duplicada. Consumir mensagens/resultados de forma explícita após rotação.
4. Copiar importação selecionada para temporário privado. Persistir token e etapa no estado salvo; não salvar JSON em Bundle. Remover temporário no cancelamento/conclusão e recuperar referência ausente com mensagem clara.
5. Rotação não cancela operação pertencente ao ViewModel. Após morte do processo, não reexecutar escrita já iniciada: mostrar interrupção e reconciliar a tela com dados atuais.
6. Limitar bloqueio aos comandos concorrentes de backup; preservar botão voltar e comportamento da navegação. Saída da tela não deve gerar notificação de sucesso para uma tentativa que falhou.
7. Migrar timestamp/logging de resultado para um único responsável e reduzir `SettingsViewModel` ao que continuar sendo preferência/configuração.
8. Localizar mensagens novas em todos os nove idiomas, incluindo sucesso parcial e documento temporário indisponível.

**Testes:** pasta sem permissão; stream nulo; falha durante escrita; fallback cancelado/bem-sucedido; toque repetido; callback repetido; rotação durante seletor/confirmação/execução; restauração com ViewModel novo; temporário inexistente; importação que muda idioma; nenhuma emissão duplicada de Activity/sucesso.

**Aceite:** arquivo efetivamente gravado é o único caminho que atualiza “última exportação”; confirmação restaurada nunca vira botão sem efeito; `BrowseScreen` fica com navegação/apresentação, sem coordenação de IO.

### P08 — Corrigir idioma e integração da Activity

**Achado:** 3. **Dependências:** P01/P07 para validar importação com mudança de idioma. **Esforço/risco:** médios.

**Arquivos:** `MainActivity.kt`, `AppLocale.kt`, `HermesAppRoot.kt`, `StringProvider.kt`, `themes.xml`, configuração de idioma/manifesto e `app/build.gradle.kts` se language splits exigir ajuste.

**Passos:**

1. Reproduzir numa API ≤32 antes de alterar integração; criar teste que verifica strings reais, não apenas o valor salvo da preferência.
2. Implementar o caminho AppCompat com tema base compatível, preservando Compose Material 3, edge-to-edge e tema claro/escuro.
3. Definir inicialização de locale antes de exibir o conteúdo localizado; remover recriações redundantes. Não usar leitura bloqueante de DataStore na main para resolver inicialização.
4. Garantir que `StringProvider.get`, formatação e categorias usem o idioma efetivo, incluindo opção “sistema”.
5. Em API 33+, conciliar preferência e configuração de idioma da plataforma sem ciclos de atualização. Definir sincronização na retomada quando a alteração vier das configurações do Android.
6. Verificar AAB para idioma não instalado inicialmente; se confirmado, desabilitar divisão por idioma como solução simples proposta.

**Testes:** API 32 e 33+; smoke adicional API 24 se imagem local disponível. Sistema PT/app EN e inverso; troca→rotação→processo novo; voltar ao sistema; alteração pelas configurações Android em API moderna; importação com mudança de idioma; ausência de loop de recriação; strings de Activity, categorias e Compose concordam.

**Aceite:** idioma funcional nas duas famílias de API e no artefato distribuível. APK universal sozinho não valida o comportamento de splits. Se a integração AppCompat exigir mudança maior do que a prevista, apresentar a alternativa de contexto localizado antes de expandir escopo.

### P09 — Migrar Events e concluir a unificação dos comandos

**Achado:** 7. **Dependências:** P05. **Esforço/risco:** médios.

**Arquivos:** `EventsViewModel.kt`, `WeeklyTrainingViewModel.kt`, `EventsUndo.kt`, `WeeklyTrainingUndo.kt`, mapeamentos de ações, módulos Hilt e testes das duas features.

**Passos:** integrar Events aos comandos P05; centralizar regras de data/categoria/ordem/metadados de `RACE_EVENT`; preservar diferenças de UI; remover implementações substituídas somente após confirmar ausência de chamadores; manter testes próprios de cada entrada e testes compartilhados de contrato.

**Testes:** criar/editar/mover/concluir/excluir o mesmo tipo de evento pelas duas telas; comparar dados e metadata; edição de evento passado sem mudar data; recusa consistente de movimento inválido; ações iguais com emissão Room atrasada; undo e confirmação de semana completa.

**Aceite:** regra compartilhada tem uma implementação; UI ainda produz suas mensagens e navegação esperadas. Não impor abstração de CRUD a desafios/recordes.

### P10 — Restaurar seleção e editor de desafios

**Achado:** 4. **Dependências:** P01. **Esforço/risco:** médios.

**Arquivos:** `ChallengesViewModel.kt`, `ChallengesScreen.kt`, `ChallengeEditorDraft.kt`, `ChallengeEditorState.kt`, testes do editor/rotas.

**Passos:**

1. Salvar ID selecionado e campos primitivos do rascunho em `SavedStateHandle` ou adaptação equivalente ao saver existente; manter uma fonte de verdade para editor.
2. Distinguir “carregando registro” de “registro ausente”. Não apagar seleção restaurada por uma emissão inicial vazia.
3. Reconciliar categoria excluída/desafio excluído com dados carregados; retornar à lista ou pedir correção do campo de forma explícita.
4. Preservar ida/volta de categorias, origem da aba, rascunho e comportamento de voltar.
5. Manter rascunho até confirmação de escrita bem-sucedida. Após falha, o usuário consegue corrigir/tentar novamente.

**Testes:** restauração de criação/edição/detalhe usando ViewModel novo; data e quantidade localizadas; categoria/desafio removidos; navegação a categorias; falha ao salvar preserva campos; recriação não cria desafio automaticamente. Não usar somente `ActivityScenario.recreate` como prova de morte do processo.

**Aceite:** editor restaurado apresenta dados corretos e não duplica criação; seleção não desaparece antes do carregamento.

### P11 — Atualizar “hoje” e separar projeções do estado de edição

**Achados:** 6/8. **Dependências:** P10 para evitar disputar a organização do ViewModel. **Esforço/risco:** médios.

**Arquivos:** `TimeModule.kt`, nova fonte de data em `core/time`, `ProgressViewModel.kt`, `EventsViewModel.kt`, `EventsScreen.kt`, `ChallengesViewModel.kt`, `ActivityViewModel.kt`, `TrophyViewModel.kt`, shell/lifecycle e testes.

**Passos:**

1. Extrair fonte observável de data baseada em Clock e zona atual. Emitir na virada do dia e refrescar na retomada; cancelamento deve liberar timer/observador.
2. Consumir data em Progresso, Events e desafios; não substituir seleção manual de semana.
3. Separar cálculo dos desafios da combinação com texto/erros de editor. Digitação altera apenas editor, não recalcula histórico.
4. Deslocar transformações puras identificadas de Activity/troféus/Progresso para dispatcher adequado, preservando ordem e atualização mais recente.
5. Nos fluxos tocados, alinhar coleta ao lifecycle sem perder confirmações/resultados/undo. Evitar refatoração global de todos os efeitos.

**Testes:** virar dia/semana sem emissão de banco; mudar dia enquanto em background; mudança de zona; início de semana configurável; cancelar coleta encerra recurso; digitar não recalcula desafios; alterações reais de progresso recalculam; múltiplas emissões não publicam resultado antigo após novo.

**Aceite:** atualização temporal correta e separação de responsabilidades verificadas. Não exigir benchmark nem apresentar ganho percentual sem medição.

### P12 — Finalizar componentes, regressão e preparação da versão

**Dependências:** P01–P11. **Esforço:** médio. **Risco:** baixo a médio.

**Arquivos:** `EventsScreen.kt` e novas seções/diálogos/rota; remanescentes de Weekly/Challenges/Browse; `LEARNING.md`, `CHANGELOG.md`, política de backup e diagrama de arquitetura quando a fronteira de componentes tiver mudado.

**Passos:**

1. Separar Events em rota, conteúdo, seções e diálogos seguindo o padrão Hermes. Concluir extrações iniciadas, sem perseguir número arbitrário de linhas.
2. Retirar código sem chamador deixado pela migração. Não remover APIs só porque parecem redundantes sem busca de uso/testes.
3. Revisar Activity, localização, DI, tratamento de falha, cancelamento e restauração nas mutações alteradas.
4. Executar suíte local completa, análise e instrumentados relevantes. Compilar release minificada/APK e AAB sem publicar.
5. Fazer smoke dos fluxos alterados em emulador, incluindo DatePicker com começo de semana configurável por causa da integração de idioma/R8.
6. Atualizar notas de aprendizado ao longo dos blocos, e ao final registrar mudanças observáveis na seção de próxima versão. Não inventar número de versão; usar a convenção existente ou definir com Rafael na preparação final.

**Aceite final:** todos os cenários obrigatórios abaixo aprovados, branch pronta para revisão, nenhuma publicação executada. Qualquer check não executado deve constar como pendente, nunca como aprovado.

## 6. Matriz obrigatória de regressão

| Fluxo | Testes locais/fakes | Room/emulador |
|---|---|---|
| Categorias | ID protegido, intenção/resultado, metadata | Reassociação/exclusão/reordenação com rollback |
| Recorde manual | Entrada atual/outra, resultado de erro | Referência manual íntegra e round-trip |
| Semana | Regras de ordem/data, undo e completion | Comando multiwrite atômico, repetição concorrente |
| Events | Paridade com Weekly e emissão atrasada | Escrita/Activity consistentes, UI continua correta |
| Backup | Falha, cancelamento, resultado parcial, consumo único | Snapshot, rollback, provedor e recriação |
| Migrações | — | Bancos antigos com dados até v7 |
| Idioma | Resolução de preferência/estado quando pura | API ≤32/33+, reabertura, AAB e strings reais |
| Desafios | Estado salvo, erro conserva rascunho | Fluxo de edição/detalhe e volta de categorias |
| Data atual | Clock, zona, retomada, cancelamento | Tela atualiza após background/virada |
| Projeções | Mesmo resultado e ordem, editor isolado | Smoke sem regressão funcional |

Usar fakes e relógio/IDs/datas fixos em testes novos. Para atomicidade, usar Room real e falhas determinísticas, por exemplo triggers que abortem uma etapa. Não medir sucesso por quantidade de chamadas a métodos privados.

## 7. Verificações e evidências por bloco

Após cada alteração: testes focados apropriados e `git diff --check`; executar gates completos quando o bloco estiver integrado. Repetir a suíte ampla somente após novas alterações relevantes ou falhas.

Comandos-base, executados via RTK conforme orientação do projeto:

```text
rtk proxy ./gradlew :app:testDebugUnitTest
rtk proxy ./gradlew :app:detekt :app:ktlintCheck :app:lintDebug --continue
rtk proxy ./gradlew :app:connectedDebugAndroidTest
rtk proxy ./gradlew :app:assembleRelease :app:bundleRelease
rtk git diff --check
```

Filtrar classes para instrumentados durante desenvolvimento; executar a seleção completa obrigatória no fechamento. Preferir cache offline quando suficiente. Instalar/usar emuladores de teste, sem operar dados pessoais. Se assinatura release não estiver disponível, distinguir validação do build minificado da geração do artefato assinado.

Evidência de cada bloco: arquivos alterados, comportamento antes/depois, testes executados, resultado, limitações e nota curta em `LEARNING.md`. Usar as skills Hermes de testes, Compose, Activity e localização conforme o bloco exigir. Fazer revisão ampla `hermes-pr-review` no encerramento.

## 8. Critérios para aprovar a execução

- [ ] Escopo das etapas 1/2 e adiamentos estão corretos.
- [ ] Transação de dados + Activity é a semântica desejada para os comandos migrados.
- [ ] Backup interrompido após morte do processo não será reexecutado automaticamente.
- [ ] Resultado parcial de importação preservará dados e explicará falha das preferências.
- [ ] Rascunhos/seleção serão restaurados; undo continuará restrito à sessão.
- [ ] Integração de idioma e estratégia simples para AAB estão de acordo.
- [ ] CI instrumentada cabe no escopo do projeto pessoal.
- [ ] Implementação seguirá na `feat/refactor`; publicação/merge continuam sendo uma etapa posterior.

**Ponto de parada atual:** revisão deste documento pelo Rafael. A execução de P01–P12 só começa após essa revisão.
