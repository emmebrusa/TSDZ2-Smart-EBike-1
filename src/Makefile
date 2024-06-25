#Makefile for STM8 Examples with SDCC compiler
#Author:	Saeid Yazdani
#Website:	WWW.EMBEDONIX.COM
#Copyright 2016
#LICENSE:	GNU-LGPL

.PHONY: all clean mkdir

#Compiler
CC = sdcc --verbose
OBJCOPY = objcopy
SIZE = stm8-size

#Platform
PLATFORM = stm8
DEVICE = STM8S105

#Product name
PNAME = main

ODIR = ../bin

#Directory for helpers
IDIR = STM8S_StdPeriph_Lib/inc
SDIR = STM8S_StdPeriph_Lib/src

# In case you ever want a different name for the main source file
MAINSRC = $(PNAME).c

# those sections are outside of the memory range (0x8000-0xFFF0) of the STM8S105
ELF_SECTIONS_TO_REMOVE = -R DATA -R INITIALIZED -R SSEG

# These are the sources that must be compiled to .rel files:
	EXTRASRCS = \
	$(SDIR)/stm8s_iwdg.c \
	$(SDIR)/stm8s_itc.c \
	$(SDIR)/stm8s_clk.c \
	$(SDIR)/stm8s_gpio.c \
	$(SDIR)/stm8s_uart2.c \
	$(SDIR)/stm8s_tim1.c \
	$(SDIR)/stm8s_tim2.c \
	$(SDIR)/stm8s_tim3.c \
    $(SDIR)/stm8s_tim4.c \
	$(SDIR)/stm8s_exti.c \
	$(SDIR)/stm8s_adc1.c \
	$(SDIR)/stm8s_flash.c \
	common.c \
	torque_sensor.c \
	uart.c \
	pwm.c \
	motor.c \
	wheel_speed_sensor.c \
	brake.c \
	pas.c \
	adc.c \
	timers.c \
	ebike_app.c \
	eeprom.c \
	lights.c \

HEADERS = common.h torque_sensor.h interrupts.h main.h uart.h pwm.h motor.h wheel_speed_sensor.h brake.h pas.h adc.h timers.h \
ebike_app.h pins.h eeprom.h lights.h config.h

# The list of .rel files can be derived from the list of their source files
RELS = $(addprefix $(ODIR)/,$(notdir $(EXTRASRCS:.c=.rel)))

override INCLUDES += -I$(IDIR) -I.
override CFLAGS += -m$(PLATFORM) -D$(DEVICE) -Ddouble=float --std-c99 --nolospre --opt-code-speed --peep-asm --peep-file peep.txt
override DEBUG_FLAGS += --out-fmt-elf --debug
LIBS = 

# This just provides the conventional target name "all"; it is optional
# Note: I assume you set PNAME via some means not exhibited in your original file
all: mkdir $(PNAME)


# How to build the overall program
$(PNAME): $(MAINSRC) $(RELS)
	$(CC) $(INCLUDES) $(CFLAGS) $(DEBUG_FLAGS) $(LIBS) $(MAINSRC) $(RELS) -o$(ODIR)/$(PNAME).elf

# 	$(SIZE) $(PNAME).elf	
	$(OBJCOPY) -g $(ODIR)/$(PNAME).elf -O ihex $(ODIR)/$(PNAME).hex $(ELF_SECTIONS_TO_REMOVE)
	cp $(ODIR)/$(PNAME).hex $(ODIR)/../$(PNAME).hex

#Rules for firmware sources
%.rel: %.c $(HEADERS)
	$(CC) -c $(INCLUDES) $(CFLAGS) $(DEBUG_FLAGS) $(LIBS) -o$(ODIR)/ $<

$(ODIR)/%.rel: %.c	
	$(CC) -c $(INCLUDES) $(CFLAGS) $(DEBUG_FLAGS) $(LIBS) -o$(ODIR)/ $<
#Rules for device driver sources
$(ODIR)/%.rel: $(SDIR)/%.c	
	$(CC) -c $(INCLUDES) $(CFLAGS) $(DEBUG_FLAGS) $(LIBS) -o$(ODIR)/ $<

# Suffixes appearing in suffix rules we care about.
# Necessary because .rel is not one of the standard suffixes.
.SUFFIXES: .c .rel

bin:
	$(OBJCOPY) -I ihex $(ODIR)/$(PNAME).hex -O binary $(ODIR)/$(PNAME).bin

backup:
	stm8flash -c stlinkv2 -p stm8s105?6 -s opt -r $(ODIR)/TSDZ_orig_opt.bin
	stm8flash -c stlinkv2 -p stm8s105?6 -r $(ODIR)/TSDZ_orig.bin
	stm8flash -c stlinkv2 -p stm8s105?6 -s eeprom -r $(ODIR)/TSDZ_orig_eeprom.bin

flash:
	stm8flash -c stlinkv2 -p stm8s105\?6 -w $(ODIR)/$(PNAME).hex

clear_eeprom:
	stm8flash -c stlinkv2 -p stm8s105\?6 -s eeprom -w data_empty.hex

clean:
	@echo "Cleaning files..."
	@rm -rf $(ODIR)
	@echo "Done."

mkdir:
	mkdir -p $(ODIR)

## uncomment to print make variables
# debug_make:
#     $(foreach var,$(.VARIABLES),$(info $(var) = $($(var))))
