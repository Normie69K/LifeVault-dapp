# LifeVault DApp 🛡️ (Incomplete)

> **⚠️ Project Status: Don't wanted to continue**
> This project was developed as a Proof of Concept (PoC) for a decentralized memory vault. It is **no longer actively maintained** and is provided as-is for educational purposes or as a reference for integrating Aptos and IPFS with Android.If you wanted to contiue and contribute to this project feel free to contribute

**LifeVault** is a decentralized application (DApp) designed to give users absolute control over their digital memories. It combines the ease of a modern Android mobile experience with the security and immutability of the Aptos blockchain and IPFS decentralized storage.

---

## 🧐 Why LifeVault?

In the current digital landscape, our most precious memories—photos, journals, and documents—are stored on centralized cloud servers. These are vulnerable to:

1. **Data Breaches:** Centralized databases are honey pots for hackers.
2. **Censorship & Deletion:** Service providers can ban accounts or delete data at will.
3. **Lack of Ownership:** You do not truly own the data you upload to social media or cloud storage.

**LifeVault** was built to solve this. We believe in **"Your life. Your memories. Your control."** By leveraging blockchain technology, we ensure that your digital footprint is immutable, censorship-resistant, and owned solely by you via your private keys.

---

## 📱 What We Built

LifeVault is a native Android application built with **Kotlin** and **Jetpack Compose**. It serves as a bridge between the user and the decentralized web.

### Key Features

* **Non-Custodial Wallet Management:**
* Users can generate a secure **Ed25519** key pair directly on the device.
* Support for BIP39 mnemonic phrase generation and recovery.
* Encrypted local storage of keys using Android's `androidx.security`.


* **Decentralized Memory Storage:**
* Users can upload "Memories" (text/photos).
* Content is hashed and stored on **IPFS** (InterPlanetary File System) for decentralized availability.
* Metadata and ownership proofs are committed to the **Aptos Blockchain** via a custom Move smart contract.


* **Blockchain Integration:**
* Direct interaction with Aptos Nodes via `OkHttp`.
* Real-time balance checks and Faucet integration (Devnet).
* Transaction simulation and execution.


* **Modern UI/UX:**
* A sleek, dark-themed interface built entirely with Jetpack Compose.
* Biometric/QR Code scanning capabilities using Google ML Kit.



---

## 📂 Project File Structure

This project follows the recommended Android MVVM (Model-View-ViewModel) architecture.

```text
LifeVault-dapp/
├── app/
│   ├── build.gradle.kts             # App-level dependencies (Compose, Coil, Retrofit, etc.)
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml  # Permissions (Camera, Internet) & Activity declaration
│           └── java/com/codebyte/lifevault_dapp/
│               ├── LifeVaultApplication.kt
│               ├── MainActivity.kt      # Entry point
│               ├── MainViewModel.kt     # Shared state management
│               │
│               ├── core/                # Core Logic & Web3 Utils
│               │   ├── AptosClient.kt   # Logic for connecting to Aptos Nodes & Faucet
│               │   ├── AptosConfig.kt   # Constants (Node URLs, Module Addresses)
│               │   ├── CryptoManager.kt # Ed25519 Key generation & Encryption
│               │   ├── IPFSClient.kt    # Logic for uploading content to IPFS
│               │   └── Web3Client.kt    # General blockchain utilities
│               │
│               ├── data/                # Data Layer
│               │   ├── BackendApiService.kt
│               │   ├── MemoryItem.kt    # Data model for a Memory
│               │   ├── MemoryRepository.kt # Local storage (SharedPrefs/Room) logic
│               │   └── NetworkModule.kt
│               │
│               ├── ui/                  # Jetpack Compose UI
│               │   ├── components/      # Reusable UI elements
│               │   │   ├── FaucetButton.kt
│               │   │   ├── MemoryCard.kt
│               │   │   ├── QRScanner.kt
│               │   │   ├── StatusCard.kt
│               │   │   ├── UploadModal.kt
│               │   │   └── WalletBalanceCard.kt
│               │   │
│               │   ├── navigation/
│               │   │   └── Navigation.kt # NavHost and Screen routes
│               │   │
│               │   ├── screens/         # Main Application Screens
│               │   │   ├── HomeScreen.kt
│               │   │   ├── InboxScreen.kt
│               │   │   ├── MemoriesListScreen.kt
│               │   │   ├── MemoryDetailScreen.kt
│               │   │   ├── OnboardingScreen.kt # Wallet Creation/Import Flow
│               │   │   ├── ProfileScreen.kt
│               │   │   ├── SendScreen.kt
│               │   │   ├── SettingsScreen.kt
│               │   │   ├── SharedScreen.kt
│               │   │   ├── TimelineScreen.kt
│               │   │   ├── UnlockScreen.kt
│               │   │   └── WalletScreen.kt
│               │   │
│               │   └── theme/           # Design System
│               │       ├── Color.kt     # Brand Colors (Orange/Black/Grey)
│               │       ├── Theme.kt
│               │       └── Type.kt
│
├── build.gradle.kts                 # Project-level build config
└── settings.gradle.kts              # Module inclusion

```

---

## 🛠️ Tech Stack & Libraries

* **Language:** Kotlin (JVM Target 17)
* **UI Framework:** Jetpack Compose (Material3)
* **Architecture:** MVVM
* **Cryptography:** Bouncy Castle (`bcprov-jdk18on`) for Ed25519 signatures.
* **Networking:**
* `Retrofit` & `OkHttp`: For REST API calls to Aptos Nodes and IPFS.
* `Gson`: JSON parsing.


* **Hardware/Sensors:**
* `CameraX` & `ML Kit`: For scanning wallet QR codes.


* **Async:** Kotlin Coroutines & Flow.
* **Image Loading:** Coil.

---

## 🚀 Getting Started

### Prerequisites

1. Android Studio Iguana or newer.
2. JDK 17.
3. An Android device or Emulator (API 26+).

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/normie69k/lifevault-dapp.git

```


2. **Open in Android Studio:**
Let Gradle sync the dependencies.
3. **Configure Environment:**
* Ensure `AptosConfig.kt` points to the correct module address for your deployed Move contract.
* *(Optional)* If using a private IPFS gateway, configure it in `IPFSClient.kt`.


4. **Run the App:**
Connect your device and press **Run**.

---

## 🔮 Future Ideas (Unimplemented)

The following features were planned but not implemented before the project was archived. Developers interested in forking this project might consider adding:

* **Full IPFS Pinning:** Currently, IPFS uploads may need a pinning service for long-term persistence.
* **Social Recovery:** Implementing a way to recover keys via trusted contacts.
* **NFT Integration:** Minting memories as NFTs on the Aptos blockchain.

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
