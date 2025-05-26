# Hoot Hoot - Interactive Quiz Game Platform

![Capture d’écran, le 2025-05-26 à 16 10 08](https://github.com/user-attachments/assets/6e717104-95fe-4509-a854-c071c17c2a90)

**Hoot Hoot** is a comprehensive quiz game platform that allows users to create, join, and participate in interactive quiz matches. The project features a rich Angular frontend, a NestJS backend, and uses Firebase for authentication and data storage.

---

## 📚 Features

* **Interactive Quiz Gameplay:** Real-time multiplayer quiz experience
* **Multiple Question Types:** Support for multiple choice, long answer and estimated answer questions
* **Game Creation Tools:** Admin interface to create and manage quiz games
* **Generative AI Tool**: Possibility to generate questions for quiz games with Gemini
* **User Authentication:** Secure login and registration system
* **Friends System:** Add friends and organize private matches
* **Real-time Chat:** In-game chat functionality
* **Money System:** Earn or lose money by playing games and transfer money to friends in need
* **User Profiles:** Customizable avatars and profile settings
* **Statistics & History:** Track your game history and performance
* **Responsive Design:** Works on desktop and mobile devices
* **Shop System:** Purchase premium avatars, themes, and wallpapers
* **Language support in french and english:** Play on Hoot Hoot in french or in english.

---

## 🖼️ Screenshots

### Home Page
![Capture d’écran, le 2025-05-26 à 16 01 48](https://github.com/user-attachments/assets/afa594c7-fa5d-4eb3-927d-b8d358916015)

### Shopping
![Capture d’écran, le 2025-05-26 à 16 02 09](https://github.com/user-attachments/assets/2e795490-b0c8-4f8e-a3f7-174c117b214d)

### Game creation
https://github.com/user-attachments/assets/6f640439-5b89-497d-ac31-0293f27a59dd

### Match room
![Capture d’écran, le 2025-05-26 à 16 13 34](https://github.com/user-attachments/assets/c8cbc9d6-2cbb-4062-869b-68e6ef4721c5)

### Gameplay
https://github.com/user-attachments/assets/3b2af44d-ba56-42fb-9608-26399a61af3d

---

## 🛠️ Technologies Used

### Frontend

* Angular
* TypeScript
* SCSS
* Firebase Authentication
* Socket.io Client
* AG Charts
* Transloco

### Backend

* NestJS
* Node.js
* Express
* TypeScript
* Socket.io
* Firebase Admin SDK

### Database & Storage

* Firebase Realtime Database
* Firebase Storage
* Firebase Authentication

### Testing

* Jasmine
* Karma

---

## 🚀 Getting Started

### Prerequisites

* Node.js (v14 or higher)
* npm (v6 or higher)
* Angular CLI
* Git

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/<yourusername>/Hoot-Hoot-QuizGame.git
   cd Hoot-Hoot-QuizGame
   ```
2. Install dependencies for both client and server:

   ```bash
   cd client
   npm ci
   cd ../server
   npm ci
   ```
3. Configure Firebase:

   * Create a Firebase project at [firebase.google.com](https://firebase.google.com)
   * Set up **Authentication**, **Realtime Database**, and **Storage**
   * Replace the Firebase configuration in `firebase-config.ts` with your project settings
   * Add your Firebase Admin credentials to `.env`

---

## 🏃‍♀️ Running the Application

### Development Mode

Start the server:

```bash
cd server
npm start
```

The server will run at `http://localhost:3000`

Start the client:

```bash
cd client
npm start
```

The client will run at `http://localhost:4200`

---

## 📱 Mobile Client

A mobile version of the application is also available for Android tablets, under the folder client-mobile. The server is the sme for regular client and desktop client.

---

## 🧪 Running Tests

### Client Tests

```bash
cd client
npm run test
```

### Server Tests

```bash
cd server
npm run test
```

---

## 📁 Project Structure

```
Hoot-Hoot-QuizGame/
├── client/          # Angular frontend for Desktop Client
├── server/          # NestJS backend
├── common/          # Shared utilities and interfaces
├── client-mobile/   # Jetpack Compose frontend for Android Client
└── README.md
```

---

## 👥 Contributors

- **[Rayan Ajakane](https://github.com/rayanajakane)**
- **[Victoria-Mae Carrière](https://github.com/Verocayden)**
- **[Hiba Chaarani](https://github.com/Hib00boo)**
- **[Sami Ait Ameur](https://github.com/Sami-Ait-Ameur)**
- **[Ikram Arroud](https://github.com/Ikramarroud)**
- **[Nada Benelfellah](https://github.com/nadabfh)**

Thanks to all our contributors! 🙌

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
