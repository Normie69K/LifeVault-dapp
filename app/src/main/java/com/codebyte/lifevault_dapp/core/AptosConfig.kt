package com.codebyte.lifevault_dapp.core

object AptosConfig {
    // Aptos Devnet Node URL
    const val NODE_URL = "https://fullnode.devnet.aptoslabs.com/v1"

    // YOUR Contract Address (From your deployment log)
    const val MODULE_ADDRESS = "0x599c19cd1f5a85d4eb4f403337bee2c26a8259b43c6cd0c9b6cdfd63d3874cc6"

    // The Module Name (Case Sensitive: Must match 'module life_vault::LifeVault')
    const val MODULE_NAME = "LifeVault"

    // Your Private Key (Keep safe!)
    const val PRIVATE_KEY = "0xdefe335acc754edec6c91604c12c5b9ebcda4c8201bb3e8fc15f3d7281f8f221"
}