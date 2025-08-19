# 📱 Palavra Criativa

O **Palavra Criativa** é um aplicativo Android que transforma **versículos bíblicos e citações** em **imagens prontas para compartilhar** em redes sociais, como **Instagram Stories, WhatsApp, Facebook e outras plataformas**.  
O app utiliza um **WebView** que carrega uma página HTML local (`index.html`), permitindo ao usuário personalizar e salvar as imagens diretamente na galeria do celular.

---

## ✨ Funcionalidades

- Digite ou cole um **versículo bíblico** ou **citação inspiradora**.  
- Personalize o texto em uma **imagem criativa e estilizada**.  
- Salve as imagens diretamente na **galeria do dispositivo**.  
- Compartilhe em **redes sociais** ou aplicativos de mensagem.  
- Layout otimizado para **celulares e tablets**.  

---

## 📂 Estrutura do Projeto

```
app/
 ├── src/
 │   ├── main/
 │   │   ├── java/dev/elizeu/palavracriativa/MainActivity.kt
 │   │   ├── res/layout/activity_main.xml
 │   │   ├── assets/index.html
 │   │   └── AndroidManifest.xml
 ├── build.gradle.kts
 └── ...
```

- `MainActivity.kt` → Código Kotlin que inicializa o WebView e gerencia permissões.  
- `activity_main.xml` → Layout principal com o componente `WebView`.  
- `index.html` → Página HTML que gera as imagens a partir dos textos inseridos.  

---

## 🛠️ Como importar no Android Studio

1. **Baixe o código do projeto** (ZIP ou Git).  
   ```bash
   git clone https://github.com/seu-repositorio/palavra-criativa.git
   ```
2. Abra o **Android Studio** → `File > Open...` → selecione a pasta do projeto.  
3. Aguarde o **Gradle Sync** finalizar.  
4. Confirme que o projeto usa **Java 17** e **Kotlin**.  

---

## 📱 Habilitando Depuração USB no Smartphone

1. Vá em: **Configurações > Sobre o telefone > Número da versão (toque 7 vezes)** → Ativa o **Modo Desenvolvedor**.  
2. Em seguida: **Configurações > Opções do desenvolvedor > Depuração USB** → Ative.  
3. Conecte o celular ao PC com um **cabo USB**.  

---

## ▶️ Testando o App no Celular

1. No Android Studio, clique em **Run ▶️**.  
2. Escolha o dispositivo conectado.  
3. O app será instalado automaticamente e abrirá no celular.  

Se aparecer um alerta no celular pedindo autorização para depuração USB → **Permita sempre**.  

---

## 📦 Gerando APK Manualmente

1. No Android Studio, vá em:  
   **Build > Build Bundle(s) / APK(s) > Build APK(s)**  
2. O APK será gerado em:  
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
3. Copie o APK para o celular e instale manualmente.  

---

## 📋 Permissões no AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="29"/>
```

- **INTERNET** → Necessária para rodar o WebView.  
- **STORAGE** → Permite salvar as imagens criadas na galeria.  

---

## ✅ Requisitos

- **Android Studio Ladybug | 2024.2.1** ou superior  
- **Java 17**  
- **Android SDK 34**  
- Celular com **Android 8.0+ (API 26)**  

---

## 📌 Observações

- O app foi criado para auxiliar **cristãos, igrejas e criadores de conteúdo** a divulgar a Palavra de Deus de forma criativa.  
- É possível **personalizar o HTML e o design das imagens** em `assets/index.html`.  
- Para publicação na Play Store, será necessário assinar o APK com uma chave própria.  

---

🙏 Que este aplicativo seja uma ferramenta para **compartilhar a fé e inspirar vidas**.  

👨‍💻 Desenvolvido por **Elizeu Barbosa Abreu**
