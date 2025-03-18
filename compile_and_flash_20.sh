#!/bin/bash
set -e

version="20.1C.6-0"
settings_date=$1

release_folder=$(pwd)/releases
backup_folder=$(pwd)/releases/backup

cd src

# Clean existing
rm -rf main.hex || true
make clean || true

# Build firmware
echo Build started...
make -j3

# Save new firmware
echo Copying firmware to release folder.
echo $release_folder/TSDZ2-$version-$settings_date.hex
mkdir -p "$release_folder"
yes | cp -rf ../bin/main.hex "$release_folder/TSDZ2-$version-$settings_date.hex"

backup=no
while true; do
	read -p "Do you want to backup the firmware ? [y/N]" yn
	case $yn in
		y ) backup=yes; break;;
		n ) break;;
		* ) break;;
esac
done

# Backup firmware
if [ "$backup" = "yes" ]; then
	echo Backup current firmware to $backup_folder/TSDZ_orig_opt-$version-$settings_date.bin
	mkdir -p $backup_folder
	make backup
	yes | cp -rf ../bin/TSDZ_orig_opt.bin $backup_folder/TSDZ_orig_opt-$version-$settings_date.bin
	yes | cp -rf ../bin/TSDZ_orig.bin $backup_folder/TSDZ_orig-$version-$settings_date.bin
	yes | cp -rf ../bin/TSDZ_orig_eeprom.bin $backup_folder/TSDZ_orig_eeprom-$version-$settings_date.bin
fi

flash=yes
while true; do
read -p "Do you want to flash the motor ? [Y/n]" yn
case $yn in
	y ) break;;
	n ) flash=no; break;;
	* ) break;;
esac
done

# Flash new firmware
if [ "$flash" = "yes" ]; then
	make clear_eeprom
	make flash
fi

echo All done !
