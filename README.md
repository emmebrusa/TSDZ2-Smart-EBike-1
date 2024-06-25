[![Build Action](../../actions/workflows/build.yaml/badge.svg)](../../actions/workflows/build.yaml)

This repository is updated by mbrusa.

This fork is based on the TSDZ2-v0.20beta1 adapted for Tongsheng protocol displays, like stock VLCD5, VLCD6, XH18 or other displays with the same protocol and 6-pin Tonsheng connector, SW102, DZ41, 850C or 860C for TSDZ2.
With these last displays, the visualization of data and errors must be checked..

Endless Sphere forum reference thread: [endless-sphere.com.](https://endless-sphere.com/forums/viewtopic.php?f=30&t=110682).

See the [wiki](https://github.com/emmebrusa/TSDZ2-Smart-EBike-1/wiki) for instructions

This ebike motor controller firmware project is to be used with the Tongsheng TSDZ2 mid drive motor.
Note: firmware can't be written to Enerdan sold TSDZ2 motors and controllers because they are equipped with V2 controller and XMC1300 microprocessor instead of STM8.

It has the following benefits compared to the stock firmware:
* The motor runs more efficient therefore it becomes more powerful and consumes less energy.
* The ebike will feel more responsive and agile.
* Using other supported displays and pheriperals will provide more functionality and features.
* Because this project is in heavy development more features will be added.

This project is being developed and maintained for free by a community of users. Some of them are developers who work professionally developing this type of technology for very well known companies.

## Building and flashing with Java tool
### Windows 
- Install [SDCC](http://sdcc.sourceforge.net/index.php#Download).
  version 4.1.0 or higher required.
- Install [ST Visual Development](http://www.st.com/en/development-tools/stvd-stm8.html).
- Install [Java](https://www.java.com/endownload/).
- Open JavaConfigurator.jar customize the parameters and click Compile & Flash
- Or use supplied .bat scripts, e.g. `src/compile_and_flash.bat` 
- With 32-bit Windows systems, replace the files in the \tools\cygwin\bin folder with those in the bin_32.zip file

### Linux and MacOS
- Install [SDCC](http://sdcc.sourceforge.net/index.php#Download).
  version 4.1.0 or higher required.
- Install Stm8flash `git clone https://github.com/vdudouyt/stm8flash.git && cd stm8flash && make && sudo make install`
- Install [Java](https://www.java.com/endownload/).
- Open JavaConfigurator.jar customize the parameters and click Compile & Flash
- And/Or use supplied shell script `compile_and_flash_20.sh` 

### For more information, go to the [wiki](https://github.com/emmebrusa/TSDZ2-Smart-EBike-1/wiki) instructions.

## Development / contributing
### Setup
1. Clone this repository
2. Install the SDCC compiler

### Debugging
- You can use [STMStdudio](https://www.st.com/en/development-tools/stm-studio-stm8.html) to plot global variables in real-time
- Or use VScode and one of the debugging setups from `.vscode/launch.json`
 - on Windows, unpack stm8-gdb.exe binary to folder located in your system's PATH environment variable
 - make sure OpenOCD 0.12 is installed
 - install [cpptools](https://marketplace.visualstudio.com/items?itemName=ms-vscode.cpptools)
 - press F5 in VScode to build, flash, and debug (`STM8-gdb` profile)



### Compile the firmware manually
- `cd src/` and use `make` or `compile.bat` to compile the firmware.

### Flashing the firmware manually
- Use `make flash` or `flash.bat` to flash the firmware.
- If you have Android with OTG you can [transfer](https://dl.google.com/tag/s/appguid%3D%7B232066FE-FF4D-4C25-83B4-3F8747CF7E3A%7D%26iid%3D%7B4A198779-3904-500B-CF23-602510C07E5B%7D%26lang%3Den%26browser%3D4%26usagestats%3D0%26appname%3DNearby%2520Better%2520Together%26needsadmin%3Dtrue/better_together/BetterTogetherSetup.exe) `main.hex`to your phone and use [Stm8 updater](https://play.google.com/store/apps/details?id=com.yatrim.stlinkp8) app to flash the `stm8s105s6`
- For advanced flashing and option bytes managment use [ST Visual Programmer STM8](https://www.st.com/en/development-tools/stvp-stm8.html). You can use preconfigured project file from `tools/ST_Vision_Programming.stp`.


### Editing environment
1. VScode can be used for the development.
  a) open project top folder as workspace
  b) install extensions from recommended popup
  c) configure Intellisense by going to Settings `ctrl+,` and specifying `@id:C_Cpp.default.systemIncludePath` according to SDCC installation folder, e.g: `C:\\Program Files\\SDCC\\include`
  d) `Ctrl+Shift+b` to build the firmware


## IMPORTANT NOTES
* Installing this firmware will void your warranty of the TSDZ2 mid drive.
* We are not responsible for any personal injuries or accidents caused by use of this firmware.
* There is no guarantee of safety using this firmware, please use it at your own risk.
* We advise you to consult the laws of your country and tailor the motor configuration accordingly.
* Please be aware of your surroundings and maintain a safe riding style.
