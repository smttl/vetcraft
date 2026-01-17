package com.vetsim.vetcraft.client;

public class ClientBankData {
    private static double balance = 0;

    public static void setBalance(double b) {
        balance = b;
    }

    public static double getBalance() {
        return balance;
    }
}
