# OreoWifiPasswords

This was Nexxado's first project (see <a href="https://github.com/Nexxado/WifiPasswords">Nexxado's GitHub branch</a>)

I adopted it and extended it (as my first android project) to get it working on Android Oreo.

#### a completely free app that shows your device saved wifi passwords.


Warning: sharing a wifi password with someone can compromise that wifi network security!

<a href="#troubleshooting">Troubleshooting</a>

## Features:
- Show your device saved wifi passwords (<u><b>ROOT REQUIRED</b></u>) 
- Add wifi passwords manually
- Hide wifi passwords in an archive list.
- Live search wifi passwords by Name & Tag.
- Optional passcode protection (style inspired by Dropbox)


#### Gesture Actions:
- Double-Tap to quickly copy a wifi password to clipboard
- Long Press to bring up the Contextual Action Bar, allowing you to tag, share, copy or archive your wifi passwords.
</br>you can then select multiple wifi passwords by simply tapping them

### External Libraries Used:

- <a href="https://github.com/JakeWharton/butterknife">Butter Knife</a> by Jake Wharton
- <a href="https://github.com/evant/gradle-retrolambda">Gradle Retrolambda Plugin</a>  by Evan Tatarka
- <a href="https://github.com/DreaminginCodeZH/MaterialProgressBar">MaterialProgressBar</a> by Zhang Hai
- <a href="https://github.com/PaoloRotolo/AppIntro">AppIntro</a> by Paolo Rotolo
- <a href="https://github.com/traex/RippleEffect">RippleEffect</a>  by Robin Chutaux

## Troubleshooting
In case the app is unable to find the file containing your saved wifi passwords please google your device's path for the saved wifi passwords file.

then insert it manually into the app's settings.

the most commonly used paths by manufacturers which the app automatically tries are: 
- /data/misc/wifi/wpa_supplicant.conf
- /data/wifi/bcm_supp.conf
- /data/misc/wifi/wpa.conf


<a href="#OreoWifiPasswords">Back to Top</a>

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


</br>
***
<a href="#OreoWifiPasswords">Back to Top</a>
