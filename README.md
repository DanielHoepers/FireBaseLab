# FireBaseLab

Aplicativo Android em Java para demonstrar Cloud Functions, Remote Config e Cloud Firestore.

## Pré-requisitos

- Android Studio com suporte ao projeto Gradle existente.
- Projeto Firebase `fir-lab-5b421`, já configurado em `app/google-services.json`.
- Acesso à internet no dispositivo para buscar valores publicados do Remote Config.
- Para publicar a função: projeto no plano **Blaze**, Node.js 22 e Firebase CLI com acesso ao projeto.

## Telas

O aplicativo inicia em `LoginActivity`. Após autenticar, abre o menu `MainActivity`, com as opções **Cloud Functions**, **Remote Config** e **Tarefas (Firestore)**. Sem uma sessão, o app apresenta login com e-mail e senha e a opção Criar conta antes de permitir acesso ao menu. Com uma sessão válida, segue diretamente para o menu.

Na tela **Cloud Functions**, toque em **Executar função**. O app chama `saudacao`, uma função HTTPS callable em `us-central1`, e mostra a resposta recebida em uma notificação. Essa demonstração não grava dados.

Na tela **Remote Config**, o título, a mensagem e a cor de destaque vêm dos parâmetros `TITULO_APP`, `MENSAGEM_APP` e `COR_DESTAQUE`. Sem valores remotos já ativados, são exibidos os valores padrão locais de `app/src/main/res/xml/remote_config_defaults.xml`. O botão **Buscar configurações** busca e ativa os valores publicados. Em compilações de depuração, o intervalo mínimo de busca é **0 segundos** para permitir vários testes durante uma apresentação; em produção, é **12 horas**. O serviço ainda pode limitar buscas excessivas.

Na tela **Tarefas (Firestore)**, entre com e-mail e senha ou toque em **Criar conta**. No console Firebase, habilite **Authentication → Sign-in method → E-mail/senha**. O app mantém a sessão para os próximos acessos e salva as tarefas em `usuarios/{uid}/tarefas`. Toque numa tarefa para concluí-la e segure para excluí-la.

## Configurar e apresentar o Remote Config

No [console do Firebase](https://console.firebase.google.com/project/fir-lab-5b421/config), abra **Remote Config**, crie estes parâmetros do tipo texto e publique as alterações:

| Parâmetro | Exemplo de valor |
| --- | --- |
| `TITULO_APP` | `Apresentação ao vivo` |
| `MENSAGEM_APP` | `Texto alterado pelo Firebase sem recompilar o app.` |
| `COR_DESTAQUE` | `#E65100` |

Para a aula, execute a variante **debug** no Android Studio. Abra **Remote Config** para mostrar os valores disponíveis (padrões locais ou valores remotos de uma busca anterior); depois toque em **Buscar configurações**. Altere os três valores no console, clique em **Publicar alterações** e toque novamente em **Buscar configurações**. O título, a mensagem e a cor mudam sem reinstalar o app. A cor altera somente a faixa de destaque, mantendo a leitura do título e do botão. Use uma cor hexadecimal no formato `#RRGGBB` ou `#AARRGGBB`.

Se a busca falhar, os valores atuais permanecem na tela e o botão permite tentar novamente. Se a cor for inválida, a faixa usa o azul padrão. A configuração segue o [guia oficial do Firebase para Android](https://firebase.google.com/docs/remote-config/android/get-started).

Para conferir a demonstração: publique os três parâmetros e busque; repita sem internet; teste uma cor inválida; coloque o app em segundo plano durante a busca e volte. O botão deve voltar a ficar disponível ao terminar a operação. Reabrir a tela mantém os valores remotos já ativados.

## Configurar o Firestore

No console, crie o banco em **Firestore Database** no modo de produção. Depois publique as regras de `firestore.rules`, que deixam cada usuário acessar só as próprias tarefas:

```sh
firebase deploy --only firestore:rules --project fir-lab-5b421
```

Também é possível colar o conteúdo do arquivo na aba **Regras** do console e clicar em **Publicar**.

## Publicar a função

Na raiz do projeto, instale as dependências do backend e publique somente a função do exemplo:

```sh
npm --prefix functions install
firebase login
firebase deploy --only functions:saudacao --project fir-lab-5b421
```

O arquivo `firebase.json` aponta para `functions/` e para as regras do Firestore. A função está em `functions/index.js`. Depois do deploy, execute o aplicativo em um dispositivo ou emulador com conexão à internet. Se a publicação falhar, confira o acesso à conta Firebase, o plano Blaze e as APIs exigidas pela CLI.

Para testar **Função em nuvem** sem publicar nem mudar de plano, abra primeiro o emulador Android. No terminal da raiz do projeto, execute:

```sh
./start-local-firebase.sh
```

Deixe esse terminal aberto, execute o app no Android Studio e toque em **Executar função**. O script conecta a porta 5001 do Android ao Mac e inicia a função local com o mesmo ID de projeto do app. Repita o comando se reiniciar o emulador. A versão de depuração usa a função local; a versão de produção usa o Firebase publicado e exige o plano Blaze para publicar Cloud Functions.

## Verificação rápida

1. Sem uma sessão, abra o app e confira o login. Após entrar ou criar conta, confira as três opções do menu. Voltar não deve reabrir o formulário de login.
2. Entre em **Remote Config** e confira os valores locais. Busque após publicar os parâmetros no console.
3. Entre em **Cloud Functions** e toque em **Executar função**. A notificação deve mostrar `Olá do Firebase Cloud Functions!` quando a função local ou publicada estiver disponível.
4. Sem uma sessão, abra o app e confira o login. Teste campos vazios, senha incorreta e falta de conexão. Entre com uma conta existente ou crie uma; confira a abertura do menu e acesse Tarefas. Reabra o app e confira que a sessão foi mantida. Teste também girar a tela e colocar o app em segundo plano durante a autenticação.
