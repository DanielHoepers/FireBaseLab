# Firebase Lab

Aplicativo Android em Java para demonstrar Cloud Functions, Remote Config e Cloud Firestore.

## Pré-requisitos

- Android Studio com suporte ao projeto Gradle existente.
- Projeto Firebase `authfirebase-efdb9`, já configurado em `app/google-services.json`.
- Acesso à internet no dispositivo para buscar valores publicados do Remote Config.
- Para publicar a função: projeto no plano **Blaze**, Node.js 22 e Firebase CLI com acesso ao projeto.

## Telas

O aplicativo abre em `MainActivity`, com as opções **Cloud Functions**, **Remote Config** e **Tarefas (Firestore)**. As telas de cadastro, login, recuperação de senha, verificação de e-mail e token foram retiradas.

Na tela **Cloud Functions**, toque em **Executar função**. O app chama `saudacao`, uma função HTTPS callable em `us-central1`, e mostra a resposta recebida em uma notificação. Essa demonstração não grava dados.

Na tela **Remote Config**, o título, a mensagem e a cor de destaque vêm dos parâmetros `titulo_app`, `mensagem_app` e `cor_destaque`. Antes da primeira busca, são exibidos os valores padrão locais de `app/src/main/res/xml/remote_config_defaults.xml`. O botão **Buscar configurações** busca e ativa os valores publicados. Em compilações de depuração, o intervalo mínimo de busca é **0 segundos** para permitir vários testes durante uma apresentação; em produção, é **12 horas**. O serviço ainda pode limitar buscas excessivas.

Na tela **Tarefas (Firestore)**, uma sessão Firebase já existente no aparelho permite criar tarefas em `usuarios/{uid}/tarefas` e ver a lista atualizar em tempo real. Toque numa tarefa para concluí-la e segure para excluí-la. O aplicativo não oferece mais uma tela para entrar nessa sessão; em uma instalação nova, Tarefas não poderá ser usada até que exista uma sessão válida.

## Configurar e apresentar o Remote Config

No [console do Firebase](https://console.firebase.google.com/project/authfirebase-efdb9/config), abra **Remote Config**, crie estes parâmetros do tipo texto e publique as alterações:

| Parâmetro | Exemplo de valor |
| --- | --- |
| `titulo_app` | `Apresentação ao vivo` |
| `mensagem_app` | `Texto alterado pelo Firebase sem recompilar o app.` |
| `cor_destaque` | `#E65100` |

Para a aula, execute a variante **debug** no Android Studio. Abra **Remote Config** para mostrar os padrões locais; depois toque em **Buscar configurações**. Altere os três valores no console, clique em **Publicar alterações** e toque novamente em **Buscar configurações**. O título, a mensagem e a cor mudam sem reinstalar o app. Use uma cor hexadecimal no formato `#RRGGBB` ou `#AARRGGBB`.

## Configurar o Firestore

No console, crie o banco em **Firestore Database** no modo de produção. Depois publique as regras de `firestore.rules`, que deixam cada usuário acessar só as próprias tarefas:

```sh
firebase deploy --only firestore:rules --project authfirebase-efdb9
```

Também é possível colar o conteúdo do arquivo na aba **Regras** do console e clicar em **Publicar**.

## Publicar a função

Na raiz do projeto, instale as dependências do backend e publique somente a função do exemplo:

```sh
npm --prefix functions install
firebase login
firebase deploy --only functions:saudacao --project authfirebase-efdb9
```

O arquivo `firebase.json` aponta para `functions/` e para as regras do Firestore. A função está em `functions/index.js`. Depois do deploy, execute o aplicativo em um dispositivo ou emulador com conexão à internet. Se a publicação falhar, confira o acesso à conta Firebase, o plano Blaze e as APIs exigidas pela CLI.

Para testar **Função em nuvem** sem publicar nem mudar de plano, abra primeiro o emulador Android. No terminal da raiz do projeto, execute:

```sh
./start-local-firebase.sh
```

Deixe esse terminal aberto, execute o app no Android Studio e toque em **Executar função**. O script conecta a porta 5001 do Android ao Mac e inicia a função local com o mesmo ID de projeto do app. Repita o comando se reiniciar o emulador. A versão de depuração usa a função local; a versão de produção usa o Firebase publicado e exige o plano Blaze para publicar Cloud Functions.

## Verificação rápida

1. Abra o app e confira as três opções do menu.
2. Entre em **Remote Config** e confira os valores locais. Busque após publicar os parâmetros no console.
3. Entre em **Cloud Functions** e toque em **Executar função**. A notificação deve mostrar `Olá do Firebase Cloud Functions!` quando a função local ou publicada estiver disponível.
4. Se houver uma sessão Firebase anterior no aparelho, abra **Tarefas (Firestore)** e confira as tarefas desse usuário.
