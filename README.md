This is a modified version of [PaooGo](https://github.com/karino2/PaooGo) for KataGo's human-like model ([discussion](https://github.com/karino2/PaooGo/issues/4)).

To build it, you need the following:

### Common build notes with the original PaooGo

Download the engines listed in the bottom of this README and place them in `android/src/main/cpp` as follows.

```
$ ls android/src/main/cpp
CMakeLists.txt  KataGo  Ray  amigogtp-1.8  gnugo-2.6  gnugo-3.8  liberty
```

You might also need to do the following steps.

```
$ cd gnugo-2.6
$ touch doc/*.info
$ ac_cv_prog_gcc=no ./configure --without-curses CFLAGS="-O2"
$ make
```

### Additional build notes for this modified version

- From KataGo's [Extra Networks](https://katagotraining.org/extra_networks/), download "Human SL Network (July 2024)" [Network file](https://media.katagotraining.org/uploaded/networks/models_extra/b18c384nbt-humanv0.bin.gz). Copy `b18c384nbt-humanv0.bin.gz` to `android/src/main/assets/katago/`, then rename `bin.gz` to `bin_gz`.
- Similarly, from KataGo's [Networks](https://katagotraining.org/networks/), download any "Network File" (e.g., [the last b10 model](https://media.katagotraining.org/uploaded/networks/models/kata1/kata1-b10c128-s1141046784-d204142634.txt.gz) for older phones). Copy it also to `android/src/main/assets/katago/`, then rename `bin.gz` to `bin_gz`, or `txt.gz` to `txt_gz`.
- Open `android/src/main/java/io/github/karino2/paoogo/goengine/katago/KataGoSetup.kt` and edit the line `MODEL_NAME="...bin.gz"` accordingly. Use `bin.gz` or `txt.gz` here (not `bin_gz` or `txt_gz).

# PaooGo

Android igo app for primer, forked from [gobandroid](https://github.com/ligi/gobandroid).

## Screenshot

![analyze](promo/screenshot_en/analyze.png)

![play](promo/screenshot_en/play.png)

![start](promo/screenshot_en/start.png)

## Used engine

- [karino2/GnuGo2Fork](https://github.com/karino2/GnuGo2Fork)
- [karino2/Ray android_fork](https://github.com/karino2/Ray/tree/android_fork)
- [karino2/LibertyFork](https://github.com/karino2/LibertyFork)
- [karino2/KataGo: android_fork](https://github.com/karino2/KataGo)
- [karino2/AmigoGtpFork](https://github.com/karino2/AmigoGtpFork)
- [karino2/gnugo_fork: android_fork](https://github.com/karino2/gnugo_fork)
