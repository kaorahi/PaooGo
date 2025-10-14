This is a modified version of [PaooGo](https://github.com/karino2/PaooGo) for KataGo's human-like model.

To build it, you need the following:

- Copy `b18c384nbt-humanv0.bin.gz` to `android/src/main/assets/katago/`, then rename `bin.gz` to `bin_gz`.
- Add the following to `cpp/android/jni_bridge.cpp` in `karino2/KataGo: android_fork`.

```cpp
void
Java_io_github_karino2_paoogo_goengine_katago_KataGoNative_setGenmoveProfile (
	JNIEnv*	env,
	jclass clasz,
	jstring profile
	)
{
  const char* profileCStr = env->GetStringUTFChars(profile, nullptr);
  SearchParams genmoveParams = g_engine->getGenmoveParams();
  genmoveParams.humanSLProfile = SGFMetadata::getProfile(profileCStr);
  g_engine->setGenmoveParamsIfChanged(genmoveParams);
}
```

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
