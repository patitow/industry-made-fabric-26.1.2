# 🛡️ Diretrizes de Segurança, Comportamento & Arquitetura — Industry Made

Este documento estabelece as **regras estritas e inegociáveis** de operação para qualquer agente de IA (Antigravity CLI / AGY / IDE) trabalhando neste repositório, com foco especial em execução segura sob o **modo Auto-Accept**.

---

## 1. Regras Críticas de Segurança & Não-Destruição

### 🛑 1.1 Proibição Total de Exclusão Sem Permissão Explícita
- **NUNCA** delete arquivos ou diretórios usando comandos de terminal (`Remove-Item`, `rm`, `del`, `rmdir`, `shutil.rmtree`, etc.) sem autorização prévia, expressa e inequívoca do usuário.
- Se a remoção de um arquivo for indispensável (ex: arquivo temporário corrompido, classe obsoleta renomeada), o agente **DEVE parar e perguntar ao usuário**, justificando a ação antes de executá-la.
- Arquivos de build gerados (`build/`, `.gradle/`, `src/main/generated/`) só podem ser limpos via tasks oficiais do Gradle (`./gradlew.bat clean`), nunca por comandos manuais de exclusão em massa.

### 🛑 1.2 Proibição de Comandos Destrutivos do Git
- **NUNCA** execute comandos que descartem alterações de trabalho não commitadas:
  - ❌ `git reset --hard`
  - ❌ `git checkout -- .` ou `git restore .`
  - ❌ `git clean -fd`
  - ❌ `git branch -D`
  - ❌ `git push --force`
- Caso haja conflito ou necessidade de reverter uma alteração feita pelo agente, realize a reversão cirurgicamente no arquivo específico ou pergunte ao usuário.
- Antes de qualquer operação ampla, verifique o estado do repositório via `git status -s`.

### 🛑 1.3 Modificações Cirúrgicas vs Sobrescrita Total
- **NUNCA** substitua arquivos de código inteiros com `write_to_file (Overwrite: true)` se pequenas modificações forem suficientes. Use `replace_file_content` para edições pontuais.
- `Overwrite: true` é aceitável apenas para:
  - Criação inicial de novos arquivos.
  - Arquivos gerados por scripts auxiliares bem definidos.
- Sempre preserve documentações, comentários, licenças e anotações pré-existentes.

### 🛑 1.4 Limites do Workspace
- **NUNCA** leia, edite ou crie arquivos fora do diretório do projeto (`D:\.Minecraft Mod Development\industry-made-fabric-26.1.2`).
- Não modifique arquivos em `%USERPROFILE%`, pastas do sistema Windows ou outros repositórios vizinhos.

---

## 2. Integridade de Build & Configurações do Projeto

### ⚙️ 2.1 Preservação do Ambiente Validado
- A stack técnica deste mod está homologada:
  - **Minecraft:** `26.1.2`
  - **Fabric Loader:** `0.19.5`
  - **Fabric Loom:** `1.18.2`
  - **Kotlin:** `2.4.20`
  - **Java / JVM:** `Java 25` (com JDK configurado em `gradle.properties`)
- **NUNCA** altere versões de dependências essenciais em `build.gradle.kts`, `gradle.properties` ou `gradle-wrapper.properties` sem alinhamento prévio com o usuário.

### ⚙️ 2.2 Validação Obrigatória a Cada Ciclo
- Nenhum ciclo de desenvolvimento deve ser dado como encerrado sem validação técnica:
  - Compilação comum: `./gradlew.bat compileKotlin` deve retornar `BUILD SUCCESSFUL`.
  - Compilação client: `./gradlew.bat compileClientKotlin` deve retornar `BUILD SUCCESSFUL`.
  - Caso haja assets/dados novos, execute `./gradlew.bat runDatagen` e confirme a geração correta dos JSONs.
- Se um build falhar, o agente deve priorizar a resolução imediata do erro antes de iniciar qualquer outra tarefa.

---

## 3. Diretrizes de Game Design (DESIGN_AND_ROADMAP.md)

1. **Zero "Caixas Mágicas Cinzas":** Todas as transformações de materiais ocorrem no mundo físico através de calor, fluxo de ar, fluidos e mecânica visível.
2. **Filosofia Sem Explosões:** Máquinas e sistemas térmicos **nunca** explodem destruindo blocos ou craterando a base do jogador. Falhas são consequências mecânicas mitigáveis (fuga de vapor com dano por proximidade, engripamento mecânico, resfriamento).
3. **Padrão Minecraft Moderno (26.x):**
   - Nunca utilize NBT legado em `ItemStack`. Utilize Data Components (`DataComponentType`).
   - Todos os blocos e itens devem ser registrados com suas chaves de recurso correspondentes (`ResourceKey.create`).
4. **Data Generation Primeiro:** Prefira sempre declarar recipes, blockstates, models, loot tables e tags via `IndustryMadeDataGenerator.kt`, mantendo a codebase enxuta e auditável.
5. **Suavidade Visual (60+ FPS):** Componentes cinéticos e animados contínuos (foles, eixos, pistões) devem implementar renderers com interpolação `partialTicks` no `BlockEntityRenderer`.
