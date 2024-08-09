/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho and Leon, 2019.
 *
 * Released under the GPL License, Version 3
 */

#include <stdint.h>
#include "main.h"
#include "stm8s.h"
#include "stm8s_flash.h"
#include "eeprom.h"
#include "ebike_app.h"


static const uint8_t ui8_default_array[EEPROM_BYTES_STORED] = 
{
  DEFAULT_VALUE_KEY,							// 0 + EEPROM_BASE_ADDRESS (Array index)
  BATTERY_CURRENT_MAX,							// 1 + EEPROM_BASE_ADDRESS
  BATTERY_LOW_VOLTAGE_CUT_OFF_X10_0,			// 2 + EEPROM_BASE_ADDRESS
  BATTERY_LOW_VOLTAGE_CUT_OFF_X10_1,			// 3 + EEPROM_BASE_ADDRESS
  WHEEL_PERIMETER_0,							// 4 + EEPROM_BASE_ADDRESS
  WHEEL_PERIMETER_1,							// 5 + EEPROM_BASE_ADDRESS
  WHEEL_MAX_SPEED,								// 6 + EEPROM_BASE_ADDRESS
  MOTOR_TYPE,									// 7 + EEPROM_BASE_ADDRESS
  AVAIABLE_FOR_FUTURE_USE,						// 8 + EEPROM_BASE_ADDRESS
  // for oem display
  MOTOR_ASSISTANCE_WITHOUT_PEDAL_ROTATION,		// 9 + EEPROM_BASE_ADDRESS
  ASSISTANCE_WITH_ERROR_ENABLED,				// 10 + EEPROM_BASE_ADDRESS
  BATTERY_SOC,									// 11 + EEPROM_BASE_ADDRESS
  ENABLE_SET_PARAMETER_ON_STARTUP,				// 12 + EEPROM_BASE_ADDRESS
  ENABLE_STREET_MODE_ON_STARTUP,				// 13 + EEPROM_BASE_ADDRESS
  RIDING_MODE_ON_STARTUP,						// 14 + EEPROM_BASE_ADDRESS
  LIGHTS_CONFIGURATION_ON_STARTUP,				// 15 + EEPROM_BASE_ADDRESS
  STARTUP_BOOST_ON_STARTUP,						// 16 + EEPROM_BASE_ADDRESS
  ENABLE_AUTO_DATA_DISPLAY,						// 17 + EEPROM_BASE_ADDRESS
  SOC_PERCENT_CALC,								// 18 + EEPROM_BASE_ADDRESS
  TORQUE_SENSOR_ADV_ON_STARTUP					// 19 + EEPROM_BASE_ADDRESS
};

static uint8_t ui8_error_number = 0;

void EEPROM_init(void)
{
  volatile uint32_t ui32_delay_counter = 0;
  
  // deinitialize EEPROM
  FLASH_DeInit();
  
  // time delay
  for (ui32_delay_counter = 0; ui32_delay_counter < 160000; ++ui32_delay_counter) {}
  
  // select and set programming time mode
  FLASH_SetProgrammingTime(FLASH_PROGRAMTIME_STANDARD); // standard programming (erase and write) time mode
  //FLASH_SetProgrammingTime(FLASH_PROGRAMTIME_TPROG); // fast programming (write only) time mode
  
  // time delay
  for (ui32_delay_counter = 0; ui32_delay_counter < 160000; ++ui32_delay_counter) {}
  
  // read key
  volatile uint8_t ui8_saved_key = FLASH_ReadByte(ADDRESS_KEY);
  
  // check if key is valid
  if (ui8_saved_key != DEFAULT_VALUE_KEY)
  {
    // set to default values
    EEPROM_controller(SET_TO_DEFAULT, 0);
  }
  
  // read from EEPROM
  EEPROM_controller(READ_FROM_MEMORY, 0);
}



void EEPROM_controller(uint8_t ui8_operation, uint8_t ui8_byte_init)
{
  struct_configuration_variables *p_configuration_variables;
  p_configuration_variables = get_configuration_variables();
  
  uint8_t ui8_array[EEPROM_BYTES_STORED];
  uint8_t ui8_temp;
  uint16_t ui16_temp;
  uint8_t ui8_i;

  // unlock memory
  FLASH_Unlock(FLASH_MEMTYPE_DATA);
  
  // wait until data EEPROM area unlocked flag is set
  while (FLASH_GetFlagStatus(FLASH_FLAG_DUL) == RESET) {}
  
  // select EEPROM operation
  switch (ui8_operation)
  {
    
    
    /********************************************************************************************************************************************************/
    
    
    case SET_TO_DEFAULT:
    
      // write array of variables to EEPROM, write key last
      for (ui8_i = EEPROM_BYTES_STORED; ui8_i > 0; ui8_i--)
      {
        // get address
        uint32_t ui32_default_address = (uint32_t) ui8_i - 1 + EEPROM_BASE_ADDRESS;
        
        // get value
        uint8_t ui8_default_variable_value = ui8_default_array[ui8_i - 1];
        
        // write variable value to EEPROM
        FLASH_ProgramByte(ui32_default_address, ui8_default_variable_value);
        
        // wait until end of programming (write or erase operation) flag is set
        while (FLASH_GetFlagStatus(FLASH_FLAG_EOP) == RESET) {}
        
        // read value from EEPROM for validation
        volatile uint8_t ui8_saved_default_value = FLASH_ReadByte(ui32_default_address);
        
        // if write was not successful, rewrite
        if (ui8_saved_default_value != ui8_default_variable_value)
		{
			// limit errors number
			ui8_error_number += 1;
			if(ui8_error_number > 3)
				ui8_display_fault_code = ERROR_WRITE_EEPROM;
			else
				ui8_i = EEPROM_BYTES_STORED;
		}
      }
      
    break;
    
    
    /********************************************************************************************************************************************************/
    
    
    case READ_FROM_MEMORY:
      
      //p_configuration_variables->ui8_motor_power_x10 = FLASH_ReadByte(ADDRESS_MOTOR_POWER_X10); // NOT USED
      p_configuration_variables->ui8_battery_current_max = FLASH_ReadByte(ADDRESS_BATTERY_CURRENT_MAX);
	  
      ui16_temp = FLASH_ReadByte(ADDRESS_BATTERY_LOW_VOLTAGE_CUT_OFF_X10_0);
      ui8_temp = FLASH_ReadByte(ADDRESS_BATTERY_LOW_VOLTAGE_CUT_OFF_X10_1);
      ui16_temp += (((uint16_t) ui8_temp << 8) & 0xff00);
      p_configuration_variables->ui16_battery_low_voltage_cut_off_x10 = ui16_temp;
      
      ui16_temp = FLASH_ReadByte(ADDRESS_WHEEL_PERIMETER_0);
      ui8_temp = FLASH_ReadByte(ADDRESS_WHEEL_PERIMETER_1);
      ui16_temp += (((uint16_t) ui8_temp << 8) & 0xff00);
      p_configuration_variables->ui16_wheel_perimeter = ui16_temp;

      p_configuration_variables->ui8_wheel_speed_max = FLASH_ReadByte(ADDRESS_WHEEL_SPEED_MAX);

      p_configuration_variables->ui8_motor_type = FLASH_ReadByte(ADDRESS_MOTOR_TYPE);
      
      p_configuration_variables->ui8_avaiable_for_future_use = FLASH_ReadByte(ADDRESS_AVAIABLE_FOR_FUTURE_USE);
      // for oem display
	  p_configuration_variables->ui8_assist_without_pedal_rotation_enabled = FLASH_ReadByte(ADDRESS_MOTOR_ASSISTANCE_WITHOUT_PEDAL_ROTATION);
	 
	  p_configuration_variables->ui8_assist_with_error_enabled = FLASH_ReadByte(ADDRESS_MOTOR_ASSISTANCE_WITH_ERROR_ENABLED);
	  p_configuration_variables->ui8_battery_SOC_percentage_8b = FLASH_ReadByte(ADDRESS_BATTERY_SOC);
	  p_configuration_variables->ui8_set_parameter_enabled = FLASH_ReadByte(ADDRESS_SET_PARAMETER_ON_STARTUP);
	  p_configuration_variables->ui8_street_mode_enabled = FLASH_ReadByte(ADDRESS_STREET_MODE_ON_STARTUP);
	  p_configuration_variables->ui8_riding_mode = FLASH_ReadByte(ADDRESS_RIDING_MODE_ON_STARTUP);
	  p_configuration_variables->ui8_lights_configuration = FLASH_ReadByte(ADDRESS_LIGHTS_CONFIGURATION_ON_STARTUP);
	  p_configuration_variables->ui8_startup_boost_enabled = FLASH_ReadByte(ADDRESS_STARTUP_BOOST_ON_STARTUP);
	  p_configuration_variables->ui8_auto_display_data_enabled = FLASH_ReadByte(ADDRESS_ENABLE_AUTO_DATA_DISPLAY);
      p_configuration_variables->ui8_soc_percent_calculation = FLASH_ReadByte(ADDRESS_SOC_PERCENT_CALC);
	  
	  p_configuration_variables->ui8_torque_sensor_adv_enabled = FLASH_ReadByte(ADDRESS_TORQUE_SENSOR_ADV_ON_STARTUP);
	  
    break;
    
    
    /********************************************************************************************************************************************************/
    
    
    case WRITE_TO_MEMORY:
    
      ui8_array[0] = DEFAULT_VALUE_KEY;
    
      //ui8_array[ADDRESS_MOTOR_POWER_X10 - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_motor_power_x10; // NOT USED
      ui8_array[ADDRESS_BATTERY_CURRENT_MAX - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_battery_current_max;
			
      ui8_array[ADDRESS_BATTERY_LOW_VOLTAGE_CUT_OFF_X10_0 - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui16_battery_low_voltage_cut_off_x10 & 255;
      ui8_array[ADDRESS_BATTERY_LOW_VOLTAGE_CUT_OFF_X10_1 - EEPROM_BASE_ADDRESS] = (p_configuration_variables->ui16_battery_low_voltage_cut_off_x10 >> 8) & 255;
      
      ui8_array[ADDRESS_WHEEL_PERIMETER_0 - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui16_wheel_perimeter & 255;
      ui8_array[ADDRESS_WHEEL_PERIMETER_1 - EEPROM_BASE_ADDRESS] = (p_configuration_variables->ui16_wheel_perimeter >> 8) & 255;
      
      ui8_array[ADDRESS_WHEEL_SPEED_MAX - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_wheel_speed_max;
      
      ui8_array[ADDRESS_MOTOR_TYPE - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_motor_type;
      
      ui8_array[ADDRESS_AVAIABLE_FOR_FUTURE_USE - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_avaiable_for_future_use;
      // for oem display
	  ui8_array[ADDRESS_MOTOR_ASSISTANCE_WITHOUT_PEDAL_ROTATION - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_assist_without_pedal_rotation_enabled;
	  
	  ui8_array[ADDRESS_MOTOR_ASSISTANCE_WITH_ERROR_ENABLED - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_assist_with_error_enabled;
	  ui8_array[ADDRESS_BATTERY_SOC - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_battery_SOC_percentage_8b;
	  ui8_array[ADDRESS_SET_PARAMETER_ON_STARTUP - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_set_parameter_enabled;
	  ui8_array[ADDRESS_STREET_MODE_ON_STARTUP - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_street_mode_enabled;
	  ui8_array[ADDRESS_RIDING_MODE_ON_STARTUP - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_riding_mode;
	  ui8_array[ADDRESS_LIGHTS_CONFIGURATION_ON_STARTUP - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_lights_configuration;
	  ui8_array[ADDRESS_STARTUP_BOOST_ON_STARTUP - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_startup_boost_enabled;
	  ui8_array[ADDRESS_ENABLE_AUTO_DATA_DISPLAY - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_auto_display_data_enabled;
	  ui8_array[ADDRESS_SOC_PERCENT_CALC - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_soc_percent_calculation;
	  ui8_array[ADDRESS_TORQUE_SENSOR_ADV_ON_STARTUP - EEPROM_BASE_ADDRESS] = p_configuration_variables->ui8_torque_sensor_adv_enabled;
	  
      // write array of variables to EEPROM
      for (ui8_i = EEPROM_BYTES_STORED; ui8_i > ui8_byte_init; ui8_i--)
      {
        // get address
        uint32_t ui32_address = (uint32_t) ui8_i - 1 + EEPROM_BASE_ADDRESS;
        
        // get value
        uint8_t ui8_variable_value = ui8_array[ui8_i - 1];
        
        // write variable value to EEPROM
        FLASH_ProgramByte(ui32_address, ui8_variable_value);
        
        // wait until end of programming (write or erase operation) flag is set
        while (FLASH_GetFlagStatus(FLASH_FLAG_EOP) == RESET) {}
        
        // read value from EEPROM for validation
        volatile uint8_t ui8_saved_value = FLASH_ReadByte(ui32_address);
        
        // if write was not successful, rewrite
		if (ui8_saved_value != ui8_variable_value)
		{
			// limit errors number
			ui8_error_number += 1;
			if(ui8_error_number > 3)
				// E09 (E08 blinking for XH18)
				ui8_display_fault_code = ERROR_WRITE_EEPROM;
			else
				ui8_i = EEPROM_BYTES_STORED;
		}
      }
      
    break;
  }
  
  // lock memory
  FLASH_Lock(FLASH_MEMTYPE_DATA);
}