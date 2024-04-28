#!/bin/bash
set -e

version="20.1C.2-3"
# optional first arg: date
settings_date="${1-$(date +%Y%m%d-%H%M%S)}"
# optional second arg: --gui (when running from configurator)
is_gui=$([ "$2" == "--gui" ] && echo "yes" || echo "no")

release_folder="$(pwd)/releases"
backup_folder="$(pwd)/releases/backup"

function check_command {
	if ! builtin type -P "$1" &> /dev/null; then
		echo "Required command '$1' was not found. Please ensure this command is installed and in your PATH."
		if [ "$is_gui" == "yes" ]; then
			# It's possible the PATH is set in a profile or rc file that the configurator didn't source.
			echo "If '$1' is installed, try running '$0' from a terminal (the PATH might be wrong in the configurator)."
			echo
			echo "PATH used: $PATH"
		fi
		exit 1
	fi
}

check_command sdcc
check_command stm8flash

cd src/controller

# Clean existing
echo "Cleaning previous output..."
rm -rf main.ihx || true
make clean || true
echo

# Build firmware
echo Build started...
make all
echo

# Save new firmware
echo Copying firmware to release folder.
echo $release_folder/TSDZ2-$version-$settings_date.hex
mkdir -p "$release_folder"
yes | cp -rf ../../bin/main.ihx "$release_folder/TSDZ2-$version-$settings_date.hex"

backup=no
# don't hang asking for input if running from configurator
if [ "$is_gui" == "no" ]; then
	while true; do
		read -p "Do you want to backup the firmware ? [y/N]" yn
		case $yn in
			y ) backup=yes; break;;
			n ) break;;
			* ) break;;
	esac
	done
fi

# Backup firmware
if [ "$backup" = "yes" ]; then
	echo Backup current firmware to $backup_folder/TSDZ_orig_opt-$version-$settings_date.bin
	mkdir -p $backup_folder
	make backup
	yes | cp -rf ../../bin/TSDZ_orig_opt.bin $backup_folder/TSDZ_orig_opt-$version-$settings_date.bin
	yes | cp -rf ../../bin/TSDZ_orig.bin $backup_folder/TSDZ_orig-$version-$settings_date.bin
	yes | cp -rf ../../bin/TSDZ_orig_eeprom.bin $backup_folder/TSDZ_orig_eeprom-$version-$settings_date.bin
fi

flash=yes
if [ "$is_gui" == "no" ]; then
	while true; do
	read -p "Do you want to flash the motor ? [Y/n]" yn
	case $yn in
		y ) break;;
		n ) flash=no; break;;
		* ) break;;
	esac
	done
fi

# Flash new firmware
if [ "$flash" = "yes" ]; then
	make clear_eeprom
	make flash
fi

echo All done !
