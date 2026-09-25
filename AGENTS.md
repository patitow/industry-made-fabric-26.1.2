# 🛡️ Diretrizes de Segurança, Comportamento & Arquitetura — Industry Made

Este arquivo replica e reforça as diretrizes de segurança definidas em [GEMINI.md](file:///D:/.Minecraft%20Mod%20Development/industry-made-fabric-26.1.2/GEMINI.md).

## 1. Segurança & Não-Destruição em Auto-Accept
- **NUNCA DELETAR NADA SEM PERMISSÃO:** É proibido executar comandos como `Remove-Item`, `rm`, `del`, `rmdir` ou scripts de deleção sem autorização expressa prévia do usuário. Se algo precisar ser apagado ou renomeado, pare e pergunte.
- **NENHUM COMANDO DESTRUTIVO DO GIT:** Proibido executar `git reset --hard`, `git checkout .`, `git clean -fd`, `git restore .` ou `git push --force`. Alterações não salvas do usuário jamais devem ser descartadas.
- **EDIÇÕES CIRÚRGICAS:** Sempre prefira `replace_file_content` para modificar apenas os blocos relevantes, evitando sobrescrever arquivos inteiros arbitrariamente.
- **ESCOPO RESTRITO:** Nunca modifique ou acesse arquivos fora do diretório deste workspace.

## 2. Padrões de Código & Build
- **VALIDAÇÃO CONTÍNUA:** Toda etapa deve compilar com sucesso (`./gradlew.bat compileKotlin` e `./gradlew.bat compileClientKotlin`).
- **STACK HOMOLOGADA:** Preservar Minecraft 26.1.2, Fabric Loader 0.19.5, Kotlin 2.4.20, Java 25. Não alterar configurações do Loom ou Gradle sem autorização.
- **DESIGN PHILOSOPHY:** Respeitar integralmente o [DESIGN_AND_ROADMAP.md](file:///D:/.Minecraft%20Mod%20Development/industry-made-fabric-26.1.2/DESIGN_AND_ROADMAP.md) (sem caixas mágicas cinzas, sem explosões punitivas que destroem a base, uso estrito de Data Components e Datagen).
