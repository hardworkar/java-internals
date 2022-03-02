package nsu.fit.javaperf.lab2;

public class Lab2 {

    int coins = 1;

    void muliplyCoins(int ratio) {
        int newcoins = coins * ratio;
        coins = newcoins;
    }
    public static void main(String[] args) {
        Lab2 l2 = new Lab2();
        l2.muliplyCoins(10);
        l2.muliplyCoins(20);
    }
}
/*
0: 2a: aload_0
1: b4 00 07: getfield #7
4: 1b: iload_1
5: 68: imul
6: 3d: istore_2
7: 2a: aload_0
8: 1c: iload_2
9: b5 00 07: putfield
12: b1: return
*/