This is a modified version of [PaooGo](https://github.com/karino2/PaooGo) for KataGo's human-like model.

To build it, you need the following:

### Common build notes with the original PaooGo

Download the engines listed in the bottom of this README and place them in `android/src/main/cpp` as follows.

```
$ ls android/src/main/cpp
CMakeLists.txt  KataGo  Ray  amigogtp-1.8  gnugo-2.6  gnugo-3.8  liberty
```

See also:

- https://github.com/karino2/PaooGo/issues/5
- https://github.com/karino2/GnuGo2Fork/issues/1

### Additional build notes for this modified version

- Download `[Network file]` in [Human SL Network (July 2024)](https://katagotraining.org/extra_networks/), copy `b18c384nbt-humanv0.bin.gz` to `android/src/main/assets/katago/`, then rename `bin.gz` to `bin_gz`.
- Replace KataGo with [kaorahi/KataGo: paoo_251015a](https://github.com/kaorahi/KataGo/tree/paoo_251015a).

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
