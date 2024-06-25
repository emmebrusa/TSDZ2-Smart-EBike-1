/*
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

/**
 *
 * @author stancecoke
 */

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ListSelectionModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
import javax.swing.JList;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import javax.swing.UIManager;

@SuppressWarnings("all")

public class TSDZ2_Configurator extends javax.swing.JFrame {

    /**
     * Creates new form TSDZ2_Configurator
     */

    private File experimentalSettingsDir;
    private File provenSettingsDir;
    private File lastSettingsFile = null;

    DefaultListModel provenSettingsFilesModel = new DefaultListModel();
    DefaultListModel experimentalSettingsFilesModel = new DefaultListModel();
    JList experimentalSettingsList = new JList(experimentalSettingsFilesModel);
    JList provenSettingsList = new JList(provenSettingsFilesModel);
    
    public class FileContainer {

		public FileContainer(File file) {
			this.file = file;
		}
		public File file;

		@Override
		public String toString() {
			return file.getName();
		}

        public File getFile() {
            return file;
        }
    }

    String[] displayDataArray = {"motor temperature", "battery SOC rem. %", "battery voltage", "battery current", "motor power", "adc throttle 8b", "adc torque sensor 10b", "pedal cadence rpm", "human power", "adc pedal torque delta", "consumed Wh", "motor ERPS", "duty cycle PWM %"};
    String[] lightModeArray = {"<br>lights ON", "<br>lights FLASHING", "lights ON and BRAKE-FLASHING brak.", "lights FLASHING and ON when braking", "lights FLASHING BRAKE-FLASHING brak.", "lights ON and ON always braking", "lights ON and BRAKE-FLASHING alw.br.", "lights FLASHING and ON always braking", "lights FLASHING BRAKE-FLASHING alw.br.", "assist without pedal rotation", "assist with sensors error", "field weakening"};

    public int[] intAdcPedalTorqueAngleAdjArray = {160, 138, 120, 107, 96, 88, 80, 74, 70, 66, 63, 59, 56, 52, 50, 47, 44, 42, 39, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16 };

    public int intMaxSpeed;
    public int intStreetSpeed;
    public int intWalkSpeed1;
    public int intWalkSpeed2;
    public int intWalkSpeed3;
    public int intWalkSpeed4;
    public int intWalkSpeedLimit;
    public int intCruiseSpeed;
    public int intCruiseSpeed1;
    public int intCruiseSpeed2;
    public int intCruiseSpeed3;
    public int intCruiseSpeed4;
    public int intTorqueAdcStep;
    public int intTorqueAdcStepCalc;
    public int intTorqueAdcOffset;
    public int intTorqueAdcMax;
    public int intTorqueAdcOnWeight;
    public int intTorqueOffsetAdj;
    public int intTorqueRangeAdj;
    public int intTorqueAngleAdj;
    public int intThrottleMode;
    public int intThrottleModeOnStreetMode;
    
    String strFileSource;
    String strFileDest;
    String strLine;
    String strTorqueOffsetAdj;
    String strTorqueRangeAdj;
    String strTorqueAngleAdj;
    
    public boolean boolDisplayTypeVLCD5;
    public boolean boolDisplayTypeVLCD6;
    public boolean boolDisplayTypeXH18;
    public boolean boolDisplayType850C;
    public boolean boolAssistStartupPower;
    public boolean boolAssistStartupTorque;
    public boolean boolAssistStartupCadence;
    public boolean boolAssistStartupEmtb;
    public boolean boolAssistStartupHybrid;
    public boolean boolOptionalAdcDisabled;
    public boolean boolOptionalAdcThrottle;
    public boolean boolOptionalAdcTemperature;
    public boolean boolStartupNone;
    public boolean boolStartupSoc;
    public boolean boolStartupVolts;
    public boolean boolSocAuto;
    public boolean boolSocWh;
    public boolean boolSocVolts;
    public boolean boolBrakeDisabled;
    public boolean boolBrakeSensor;
    public boolean boolCoasterBrake;
    public boolean boolTemperatureSwitch;
    public boolean boolTemperatureSensorType;
    public boolean boolStreetThrottle;
    public boolean boolThrottleLegal;
    public boolean boolUnitsKilometers;
    public boolean boolUnitsMiles;
    public boolean boolAlternativeMiles;
    
    public static final int VLCD5 = 0;
    public static final int VLCD6 = 1;
    public static final int XH18 = 2;
    public static final int C850 = 3;
    public static final int KILOMETERS = 0;
    public static final int MILES = 1;
    public static final int ALTERNATIVE_MILES = 2;
    public static final int POWER = 0;
    public static final int TORQUE = 1;
    public static final int CADENCE = 2;
    public static final int EMTB = 3;
    public static final int HYBRID = 4;
    public static final int DISABLED = 0;
    public static final int THROTTLE = 1;
    public static final int TEMPERATURE = 2;
    public static final int PEDALING = 1;
    public static final int UNCONDITIONAL = 4;
    public static final int NONE = 0;
    public static final int SOC = 1;
    public static final int VOLTS = 2;
    public static final int AUTO = 0;
    public static final int WH = 1;
    public static final int BRAKE_SENSOR = 1;
    public static final int COASTER_BRAKE = 2;
    public static final int TEMPERATURE_SWITCH = 3;
    public static final int LM35 = 0;
    public static final int TMP36 = 1;
    public static final int EMTB_ASSIST_MIN = 21;
    
    public static final int WEIGHT_ON_PEDAL = 25; // kg
    public static final int MIDDLE_OFFSET_ADJ = 20;
    public static final int OFFSET_MAX_VALUE = 34; // MIDDLE_OFFSET_ADJ * 2 - 6 (ADC_TORQUE_SENSOR_CALIBRATION_OFFSET)
    public static final int MIDDLE_RANGE_ADJ = 20;
    public static final int MIDDLE_ANGLE_ADJ = 20;

    public void loadSettings(File f) throws IOException {

        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            RB_MOTOR_36V.setSelected(Boolean.parseBoolean(in.readLine()));
            RB_MOTOR_48V.setSelected(Boolean.parseBoolean(in.readLine()));
            CB_TORQUE_CALIBRATION.setSelected(Boolean.parseBoolean(in.readLine()));
            TF_MOTOR_ACC.setText(in.readLine());
            CB_ASS_WITHOUT_PED.setSelected(Boolean.parseBoolean(in.readLine()));
            TF_ASS_WITHOUT_PED_THRES.setText(in.readLine());
            intTorqueAdcStep = Integer.parseInt(in.readLine());
            TF_TORQUE_ADC_MAX.setText(in.readLine());
            TF_BOOST_TORQUE_FACTOR.setText(in.readLine());
            TF_MOTOR_BLOCK_TIME.setText(in.readLine());
            TF_MOTOR_BLOCK_CURR.setText(in.readLine());
            TF_MOTOR_BLOCK_ERPS.setText(in.readLine());
            TF_BOOST_CADENCE_STEP.setText(in.readLine());
            TF_BAT_CUR_MAX.setText(in.readLine());
            TF_BATT_POW_MAX.setText(in.readLine());
            TF_BATT_CAPACITY.setText(in.readLine());
            TF_BATT_NUM_CELLS.setText(in.readLine());
            TF_MOTOR_DEC.setText(in.readLine());
            TF_BATT_VOLT_CUT_OFF.setText(in.readLine());
            TF_BATT_VOLT_CAL.setText(in.readLine());
            TF_BATT_CAPACITY_CAL.setText(in.readLine());
            TF_BAT_CELL_OVER.setText(in.readLine());
            TF_BAT_CELL_SOC.setText(in.readLine());
            TF_BAT_CELL_FULL.setText(in.readLine());
            TF_BAT_CELL_3_4.setText(in.readLine());
            TF_BAT_CELL_2_4.setText(in.readLine());
            TF_BAT_CELL_1_4.setText(in.readLine());
            TF_BAT_CELL_5_6.setText(in.readLine());
            TF_BAT_CELL_4_6.setText(in.readLine());
            TF_BAT_CELL_3_6.setText(in.readLine());
            TF_BAT_CELL_2_6.setText(in.readLine());
            TF_BAT_CELL_1_6.setText(in.readLine());
            TF_BAT_CELL_EMPTY.setText(in.readLine());
            TF_WHEEL_CIRCUMF.setText(in.readLine());
            intMaxSpeed = Integer.parseInt(in.readLine());
            CB_LIGHTS.setSelected(Boolean.parseBoolean(in.readLine()));
            CB_WALK_ASSIST_ENABLED.setSelected(Boolean.parseBoolean(in.readLine()));
            boolBrakeSensor = Boolean.parseBoolean(in.readLine());
            boolOptionalAdcDisabled = Boolean.parseBoolean(in.readLine());
            boolOptionalAdcThrottle = Boolean.parseBoolean(in.readLine());
            boolOptionalAdcTemperature = Boolean.parseBoolean(in.readLine());
            CB_STREET_MODE_ON_START.setSelected(Boolean.parseBoolean(in.readLine()));
            CB_SET_PARAM_ON_START.setSelected(Boolean.parseBoolean(in.readLine()));
            CB_ODO_COMPENSATION.setSelected(Boolean.parseBoolean(in.readLine()));
            CB_STARTUP_BOOST_ON_START.setSelected(Boolean.parseBoolean(in.readLine()));
            CB_TOR_SENSOR_ADV.setSelected(Boolean.parseBoolean(in.readLine()));
            TF_LIGHT_MODE_ON_START.setText(in.readLine());
            boolAssistStartupPower = Boolean.parseBoolean(in.readLine());
            boolAssistStartupTorque = Boolean.parseBoolean(in.readLine());
            boolAssistStartupCadence = Boolean.parseBoolean(in.readLine());
            boolAssistStartupEmtb = Boolean.parseBoolean(in.readLine());
            TF_LIGHT_MODE_1.setText(in.readLine());
            TF_LIGHT_MODE_2.setText(in.readLine());
            TF_LIGHT_MODE_3.setText(in.readLine());
            CB_STREET_POWER_LIM.setSelected(Boolean.parseBoolean(in.readLine()));
            TF_STREET_POWER_LIM.setText(in.readLine());
            intStreetSpeed = Integer.parseInt(in.readLine());
            boolStreetThrottle = Boolean.parseBoolean(in.readLine());
            CB_STREET_CRUISE.setSelected(Boolean.parseBoolean(in.readLine()));
            TF_ADC_THROTTLE_MIN.setText(in.readLine());
            TF_ADC_THROTTLE_MAX.setText(in.readLine());
            TF_TEMP_MIN_LIM.setText(in.readLine());
            TF_TEMP_MAX_LIM.setText(in.readLine());
            CB_TEMP_ERR_MIN_LIM.setSelected(Boolean.parseBoolean(in.readLine()));
            boolDisplayTypeVLCD6 = Boolean.parseBoolean(in.readLine());
            boolDisplayTypeVLCD5 = Boolean.parseBoolean(in.readLine());
            boolDisplayTypeXH18 = Boolean.parseBoolean(in.readLine());
            RB_DISPLAY_WORK_ON.setSelected(Boolean.parseBoolean(in.readLine()));
            RB_DISPLAY_ALWAY_ON.setSelected(Boolean.parseBoolean(in.readLine()));
            CB_MAX_SPEED_DISPLAY.setSelected(Boolean.parseBoolean(in.readLine()));
            TF_DELAY_MENU.setText(in.readLine());
            boolCoasterBrake = Boolean.parseBoolean(in.readLine());
            TF_COASTER_BRAKE_THRESHOLD.setText(in.readLine());
            CB_AUTO_DISPLAY_DATA.setSelected(Boolean.parseBoolean(in.readLine()));
            CB_STARTUP_ASSIST_ENABLED.setSelected(Boolean.parseBoolean(in.readLine()));
            TF_DELAY_DATA_1.setText(in.readLine());
            TF_DELAY_DATA_2.setText(in.readLine());
            TF_DELAY_DATA_3.setText(in.readLine());
            TF_DELAY_DATA_4.setText(in.readLine());
            TF_DELAY_DATA_5.setText(in.readLine());
            TF_DELAY_DATA_6.setText(in.readLine());
            TF_DATA_1.setText(in.readLine());
            TF_DATA_2.setText(in.readLine());
            TF_DATA_3.setText(in.readLine());
            TF_DATA_4.setText(in.readLine());
            TF_DATA_5.setText(in.readLine());
            TF_DATA_6.setText(in.readLine());
            TF_POWER_ASS_1.setText(in.readLine());
            TF_POWER_ASS_2.setText(in.readLine());
            TF_POWER_ASS_3.setText(in.readLine());
            TF_POWER_ASS_4.setText(in.readLine());
            TF_TORQUE_ASS_1.setText(in.readLine());
            TF_TORQUE_ASS_2.setText(in.readLine());
            TF_TORQUE_ASS_3.setText(in.readLine());
            TF_TORQUE_ASS_4.setText(in.readLine());
            TF_CADENCE_ASS_1.setText(in.readLine());
            TF_CADENCE_ASS_2.setText(in.readLine());
            TF_CADENCE_ASS_3.setText(in.readLine());
            TF_CADENCE_ASS_4.setText(in.readLine());
            TF_EMTB_ASS_1.setText(in.readLine());
            TF_EMTB_ASS_2.setText(in.readLine());
            TF_EMTB_ASS_3.setText(in.readLine());
            TF_EMTB_ASS_4.setText(in.readLine());
            intWalkSpeed1 =  Integer.parseInt(in.readLine());
            intWalkSpeed2 =  Integer.parseInt(in.readLine());
            intWalkSpeed3 =  Integer.parseInt(in.readLine());
            intWalkSpeed4 =  Integer.parseInt(in.readLine());
            intWalkSpeedLimit =  Integer.parseInt(in.readLine());
            CB_WALK_TIME_ENA.setSelected(Boolean.parseBoolean(in.readLine()));
            TF_WALK_ASS_TIME.setText(in.readLine());
            intCruiseSpeed1 =  Integer.parseInt(in.readLine());
            intCruiseSpeed2 =  Integer.parseInt(in.readLine());
            intCruiseSpeed3 =  Integer.parseInt(in.readLine());
            intCruiseSpeed4 =  Integer.parseInt(in.readLine());
            CB_CRUISE_WHITOUT_PEDALING.setSelected(Boolean.parseBoolean(in.readLine()));
            intCruiseSpeed =  Integer.parseInt(in.readLine());
            TF_TORQ_ADC_OFFSET.setText(in.readLine());
            TF_NUM_DATA_AUTO_DISPLAY.setText(in.readLine());
            boolUnitsKilometers = Boolean.parseBoolean(in.readLine());
            boolUnitsMiles = Boolean.parseBoolean(in.readLine());
            TF_ASSIST_THROTTLE_MIN.setText(in.readLine());
            TF_ASSIST_THROTTLE_MAX.setText(in.readLine());
            CB_STREET_WALK.setSelected(Boolean.parseBoolean(in.readLine()));
            boolAssistStartupHybrid = Boolean.parseBoolean(in.readLine());
            boolStartupNone = Boolean.parseBoolean(in.readLine());
            boolStartupSoc = Boolean.parseBoolean(in.readLine());
            boolStartupVolts = Boolean.parseBoolean(in.readLine());
            CB_FIELD_WEAKENING_ENABLED.setSelected(Boolean.parseBoolean(in.readLine()));
            strTorqueOffsetAdj = in.readLine();
            strTorqueRangeAdj = in.readLine();
            strTorqueAngleAdj = in.readLine();
            TF_TORQ_PER_ADC_STEP_ADV.setText(in.readLine());
            boolSocAuto = Boolean.parseBoolean(in.readLine());
            boolSocWh = Boolean.parseBoolean(in.readLine());
            boolSocVolts = Boolean.parseBoolean(in.readLine());
            CB_ADC_STEP_ESTIM.setSelected(Boolean.parseBoolean(in.readLine()));
            RB_BOOST_AT_ZERO_CADENCE.setSelected(Boolean.parseBoolean(in.readLine()));
            RB_BOOST_AT_ZERO_SPEED.setSelected(Boolean.parseBoolean(in.readLine()));
            boolDisplayType850C = Boolean.parseBoolean(in.readLine());
            boolThrottleLegal = Boolean.parseBoolean(in.readLine());
            
            strLine = in.readLine();
            if (strLine != null) {
                boolTemperatureSwitch = Boolean.parseBoolean(strLine);
                RB_eMTB_POWER.setSelected(Boolean.parseBoolean(in.readLine()));
                RB_eMTB_TORQUE.setSelected(Boolean.parseBoolean(in.readLine()));
                CB_SMOOTH_START_ENABLED.setSelected(Boolean.parseBoolean(in.readLine()));
                TF_SMOOTH_START_RAMP.setText(in.readLine());
                boolTemperatureSensorType = Boolean.parseBoolean(in.readLine());
                CB_CRUISE_ENABLED.setSelected(Boolean.parseBoolean(in.readLine()));
                intThrottleMode = Integer.parseInt(in.readLine());
                intThrottleModeOnStreetMode = Integer.parseInt(in.readLine());
                TF_ASS_LEVELS_1_OF_5.setText(in.readLine());
                boolAlternativeMiles = Boolean.parseBoolean(in.readLine());
            }
            else {
                boolTemperatureSwitch = false;
                RB_eMTB_POWER.setSelected(true);
                RB_eMTB_TORQUE.setSelected(false);
                CB_SMOOTH_START_ENABLED.setSelected(true);
                TF_SMOOTH_START_RAMP.setText("35");
                boolTemperatureSensorType = false;
                CB_CRUISE_ENABLED.setSelected(true);
                TF_ASS_LEVELS_1_OF_5.setText("60");
                boolAlternativeMiles = false;
                
                if (boolOptionalAdcThrottle) {
                    intThrottleMode = UNCONDITIONAL;
                }            
                else {
                    intThrottleMode = DISABLED;
                }
                
                if ((boolOptionalAdcThrottle)&&(boolStreetThrottle)) {
                    if (boolThrottleLegal) {
                        intThrottleModeOnStreetMode = PEDALING;
                    }
                    else {
                        intThrottleModeOnStreetMode = UNCONDITIONAL;
                    }
                }
                else {
                    intThrottleModeOnStreetMode = DISABLED;
                }
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Corrupt .ini file or invalid data", "TSDZ2 Patameter Configurator", JOptionPane.INFORMATION_MESSAGE);
        }

                if ((!RB_BOOST_AT_ZERO_CADENCE.isSelected()) && (!RB_BOOST_AT_ZERO_SPEED.isSelected())) {
                    RB_BOOST_AT_ZERO_CADENCE.setSelected(true); }

                TF_TORQ_PER_ADC_STEP.setText(String.valueOf(intTorqueAdcStep));

                if (strTorqueOffsetAdj != null) {
                    intTorqueOffsetAdj = Integer.parseInt(strTorqueOffsetAdj); }
                else {
                    intTorqueOffsetAdj = MIDDLE_OFFSET_ADJ; }

                if (intTorqueOffsetAdj == MIDDLE_OFFSET_ADJ) {
                    TF_TORQ_ADC_OFFSET_ADJ.setText("0"); }
                else if (intTorqueOffsetAdj < MIDDLE_OFFSET_ADJ) {
                    TF_TORQ_ADC_OFFSET_ADJ.setText("-" + String.valueOf(MIDDLE_OFFSET_ADJ - intTorqueOffsetAdj)); }
                else {
                    TF_TORQ_ADC_OFFSET_ADJ.setText(String.valueOf(intTorqueOffsetAdj - MIDDLE_OFFSET_ADJ)); }

                if (strTorqueRangeAdj != null) {
                    intTorqueRangeAdj = Integer.parseInt(strTorqueRangeAdj); }
                else {
                    intTorqueRangeAdj = MIDDLE_RANGE_ADJ; }

                if (intTorqueRangeAdj == MIDDLE_RANGE_ADJ) {
                    TF_TORQ_ADC_RANGE_ADJ.setText("0"); }
                else if (intTorqueRangeAdj < MIDDLE_RANGE_ADJ) {
                    TF_TORQ_ADC_RANGE_ADJ.setText("-" + String.valueOf(MIDDLE_RANGE_ADJ - intTorqueRangeAdj)); }
                else {
                    TF_TORQ_ADC_RANGE_ADJ.setText(String.valueOf(intTorqueRangeAdj - MIDDLE_RANGE_ADJ)); }

                if (strTorqueAngleAdj != null) {
                    intTorqueAngleAdj = Integer.parseInt(strTorqueAngleAdj); }
                else {
                    intTorqueAngleAdj = MIDDLE_ANGLE_ADJ; }

                if (intTorqueAngleAdj == MIDDLE_ANGLE_ADJ) {
                    TF_TORQ_ADC_ANGLE_ADJ.setText("0"); }
                else if (intTorqueAngleAdj < MIDDLE_ANGLE_ADJ) {
                    TF_TORQ_ADC_ANGLE_ADJ.setText("-" + String.valueOf(MIDDLE_ANGLE_ADJ - intTorqueAngleAdj)); }
                else {
                    TF_TORQ_ADC_ANGLE_ADJ.setText(String.valueOf(intTorqueAngleAdj - MIDDLE_ANGLE_ADJ)); }
                
                if (boolAssistStartupPower) {
                    JCB_ASSIST_MODE_ON_STARTUP.setSelectedIndex(POWER);
                }    
                else if (boolAssistStartupTorque) {
                    JCB_ASSIST_MODE_ON_STARTUP.setSelectedIndex(TORQUE);
                }
                else if (boolAssistStartupCadence) {
                    JCB_ASSIST_MODE_ON_STARTUP.setSelectedIndex(CADENCE);
                }
                else if (boolAssistStartupEmtb) {
                    JCB_ASSIST_MODE_ON_STARTUP.setSelectedIndex(EMTB);
                }
                else if (boolAssistStartupHybrid) {
                    JCB_ASSIST_MODE_ON_STARTUP.setSelectedIndex(HYBRID);
                }
                
                if (boolDisplayTypeVLCD5) {
                    JCB_DISPLAY_TYPE.setSelectedIndex(VLCD5);
                }    
                else if (boolDisplayTypeVLCD6) {
                    JCB_DISPLAY_TYPE.setSelectedIndex(VLCD6);
                }
                else if (boolDisplayTypeXH18) {
                    JCB_DISPLAY_TYPE.setSelectedIndex(XH18);
                }
                else if (boolDisplayType850C) {
                    JCB_DISPLAY_TYPE.setSelectedIndex(C850);
                }
                
                if (boolStartupNone) {
                    JCB_DATA_ON_STARTUP.setSelectedIndex(NONE);
                }    
                else if (boolStartupSoc) {
                    JCB_DATA_ON_STARTUP.setSelectedIndex(SOC);
                }
                else if (boolStartupVolts) {
                    JCB_DATA_ON_STARTUP.setSelectedIndex(VOLTS);
                }

                if (boolSocAuto) {
                    JCB_SOC_CALC.setSelectedIndex(AUTO);
                }    
                else if (boolSocWh) {
                    JCB_SOC_CALC.setSelectedIndex(WH);
                }
                else if (boolSocVolts) {
                    JCB_SOC_CALC.setSelectedIndex(VOLTS);
                }
                
                if (boolTemperatureSensorType) {
                    JCB_TEMP_SENSOR_TYPE.setSelectedIndex(TMP36);
                }    
                else {
                    JCB_TEMP_SENSOR_TYPE.setSelectedIndex(LM35);
                }
                
                if (boolBrakeSensor) {
                    JCB_BRAKE_FEATURE.setSelectedIndex(BRAKE_SENSOR);
                }
                else if (boolCoasterBrake) {
                    JCB_BRAKE_FEATURE.setSelectedIndex(COASTER_BRAKE);
                }
                else if (boolTemperatureSwitch) {
                    JCB_BRAKE_FEATURE.setSelectedIndex(TEMPERATURE_SWITCH);
                }
                else {
                    JCB_BRAKE_FEATURE.setSelectedIndex(DISABLED);
                    boolBrakeDisabled = true;
                }
                
                if (boolOptionalAdcDisabled) {
                    JCB_OPTIONAL_ADC.setSelectedIndex(DISABLED);
                }    
                else if (boolOptionalAdcThrottle) {
                    JCB_OPTIONAL_ADC.setSelectedIndex(THROTTLE);
                }
                else if (boolOptionalAdcTemperature) {
                    JCB_OPTIONAL_ADC.setSelectedIndex(TEMPERATURE);
                }
                
                JCB_THROTTLE_MODE.setSelectedIndex(intThrottleMode);
                JCB_THROTTLE_MODE_STREET.setSelectedIndex(intThrottleModeOnStreetMode);
                
                jLabelTHROTTLE_ASSIST_MIN.setVisible(false);
                jLabelTHROTTLE_ASSIST_MAX.setVisible(false);
                jLabelMOTOR_BLOCK_CURR.setVisible(false);
                jLabelMOTOR_BLOCK_ERPS.setVisible(false);
                jLabelTF_MOTOR_BLOCK_TIME.setVisible(false);
                TF_ASSIST_THROTTLE_MIN.setVisible(false);
                TF_ASSIST_THROTTLE_MAX.setVisible(false);
                TF_MOTOR_BLOCK_CURR.setVisible(false);
                TF_MOTOR_BLOCK_ERPS.setVisible(false);
                TF_MOTOR_BLOCK_TIME.setVisible(false);

                jLabelData1.setText("Data 1 - " + displayDataArray[Integer.parseInt(TF_DATA_1.getText())]);
                jLabelData2.setText("Data 2 - " + displayDataArray[Integer.parseInt(TF_DATA_2.getText())]);
                jLabelData3.setText("Data 3 - " + displayDataArray[Integer.parseInt(TF_DATA_3.getText())]);
                jLabelData4.setText("Data 4 - " + displayDataArray[Integer.parseInt(TF_DATA_4.getText())]);
                jLabelData5.setText("Data 5 - " + displayDataArray[Integer.parseInt(TF_DATA_5.getText())]);
                jLabelData6.setText("Data 6 - " + displayDataArray[Integer.parseInt(TF_DATA_6.getText())]);

                jLabelLights0.setText("<html>Lights mode on startup " + lightModeArray[Integer.parseInt(TF_LIGHT_MODE_ON_START.getText())] + "</html>");
                jLabelLights1.setText("<html>Mode 1 - " + lightModeArray[Integer.parseInt(TF_LIGHT_MODE_1.getText())] + "</html>");
                jLabelLights2.setText("<html>Mode 2 - " + lightModeArray[Integer.parseInt(TF_LIGHT_MODE_2.getText())] + "</html>");
                jLabelLights3.setText("<html>Mode 3 - " + lightModeArray[Integer.parseInt(TF_LIGHT_MODE_3.getText())] + "</html>");

                if (Integer.parseInt(TF_EMTB_ASS_1.getText()) < 21) {
                    TF_EMTB_ASS_1.setText(String.valueOf((Integer.parseInt(TF_EMTB_ASS_1.getText()))*127/10));
                }
                if (Integer.parseInt(TF_EMTB_ASS_2.getText()) < 21) {
                    TF_EMTB_ASS_2.setText(String.valueOf((Integer.parseInt(TF_EMTB_ASS_2.getText()))*127/10));
                }
                if (Integer.parseInt(TF_EMTB_ASS_3.getText()) < 21) {
                    TF_EMTB_ASS_3.setText(String.valueOf((Integer.parseInt(TF_EMTB_ASS_3.getText()))*127/10));
                }
                if (Integer.parseInt(TF_EMTB_ASS_4.getText()) < 21) {
                    TF_EMTB_ASS_4.setText(String.valueOf((Integer.parseInt(TF_EMTB_ASS_4.getText()))*127/10));
                }
                
                if (boolUnitsKilometers) {
                    jLabelMaxSpeed.setText("Max speed offroad mode (km/h)");
                    jLabelStreetSpeed.setText("Street speed limit (km/h)");
                    jLabelCruiseSpeedUnits.setText("km/h");
                    jLabelWalkSpeedUnits.setText("km/h x10");
                    TF_MAX_SPEED.setText(String.valueOf(intMaxSpeed));
                    TF_STREET_SPEED_LIM.setText(String.valueOf(intStreetSpeed));
                    TF_WALK_ASS_SPEED_1.setText(String.valueOf(intWalkSpeed1));
                    TF_WALK_ASS_SPEED_2.setText(String.valueOf(intWalkSpeed2));
                    TF_WALK_ASS_SPEED_3.setText(String.valueOf(intWalkSpeed3));
                    TF_WALK_ASS_SPEED_4.setText(String.valueOf(intWalkSpeed4));
                    TF_WALK_ASS_SPEED_LIMIT.setText(String.valueOf(intWalkSpeedLimit));
                    TF_CRUISE_SPEED_ENA.setText(String.valueOf(intCruiseSpeed));
                    TF_CRUISE_ASS_1.setText(String.valueOf(intCruiseSpeed1));
                    TF_CRUISE_ASS_2.setText(String.valueOf(intCruiseSpeed2));
                    TF_CRUISE_ASS_3.setText(String.valueOf(intCruiseSpeed3));
                    TF_CRUISE_ASS_4.setText(String.valueOf(intCruiseSpeed4));
                    JCB_UNITS_TYPE.setSelectedIndex(KILOMETERS);
		}
                else {
                    jLabelMaxSpeed.setText("Max speed offroad mode (mph)");
                    jLabelStreetSpeed.setText("Street speed limit (mph)");
                    jLabelCruiseSpeedUnits.setText("mph");
                    jLabelWalkSpeedUnits.setText("mph x10");
                    TF_MAX_SPEED.setText(String.valueOf((intMaxSpeed * 10 + 5) / 16));
                    TF_STREET_SPEED_LIM.setText(String.valueOf((intStreetSpeed * 10 + 5) / 16));
                    TF_WALK_ASS_SPEED_1.setText(String.valueOf((intWalkSpeed1 * 10 + 5) / 16));
                    TF_WALK_ASS_SPEED_2.setText(String.valueOf((intWalkSpeed2 * 10 + 5) / 16));
                    TF_WALK_ASS_SPEED_3.setText(String.valueOf((intWalkSpeed3 * 10 + 5) / 16));
                    TF_WALK_ASS_SPEED_4.setText(String.valueOf((intWalkSpeed4 * 10 + 5) / 16));
                    TF_WALK_ASS_SPEED_LIMIT.setText(String.valueOf((intWalkSpeedLimit * 10 + 5) / 16));
                    TF_CRUISE_SPEED_ENA.setText(String.valueOf((intCruiseSpeed * 10 + 5) / 16));
                    TF_CRUISE_ASS_1.setText(String.valueOf((intCruiseSpeed1 * 10 + 5) / 16));
                    TF_CRUISE_ASS_2.setText(String.valueOf((intCruiseSpeed2 * 10 + 5) / 16));
                    TF_CRUISE_ASS_3.setText(String.valueOf((intCruiseSpeed3 * 10 + 5) / 16));
                    TF_CRUISE_ASS_4.setText(String.valueOf((intCruiseSpeed4 * 10 + 5) / 16));
                }
                if (boolUnitsMiles) {
                    JCB_UNITS_TYPE.setSelectedIndex(MILES);
                }
                else if (boolAlternativeMiles) {
                    JCB_UNITS_TYPE.setSelectedIndex(ALTERNATIVE_MILES);
                }

            try {
		BufferedReader br = new BufferedReader (new FileReader("commits.txt"));
                LB_LAST_COMMIT.setText("<html>" + br.readLine() + "</html>");
                br.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "ommits.txt file not found" , "TSDZ2 Patameter Configurator", JOptionPane.INFORMATION_MESSAGE);
            }
    }

    public void AddListItem(File newFile) {

        experimentalSettingsFilesModel.add(0, new FileContainer(newFile));

        expSet.setSelectedIndex(0);
        expSet.repaint();
        // JOptionPane.showMessageDialog(null,experimentalSettingsFilesModel.toString(),"Titel", JOptionPane.PLAIN_MESSAGE);
    }

    public TSDZ2_Configurator() {
        initComponents();

        this.setLocationRelativeTo(null);

        // update lists
      
        experimentalSettingsDir = new File(Paths.get(".").toAbsolutePath().normalize().toString());
      
	while (!Arrays.asList(experimentalSettingsDir.list()).contains("experimental settings")) {
            experimentalSettingsDir = experimentalSettingsDir.getParentFile();
        }
	
        provenSettingsDir = new File(experimentalSettingsDir.getAbsolutePath() + File.separator + "proven settings");
	experimentalSettingsDir = new File(experimentalSettingsDir.getAbsolutePath() + File.separator + "experimental settings");


        File[] provenSettingsFiles = provenSettingsDir.listFiles();
        Arrays.sort(provenSettingsFiles);
		for (File file : provenSettingsFiles) {
			provenSettingsFilesModel.addElement(new TSDZ2_Configurator.FileContainer(file));

			if (lastSettingsFile == null) {
				lastSettingsFile = file;
			} else {
				if(file.lastModified()>lastSettingsFile.lastModified()){
					lastSettingsFile = file;
				}
			}
		}


        File[] experimentalSettingsFiles = experimentalSettingsDir.listFiles();
            Arrays.sort(experimentalSettingsFiles, Collections.reverseOrder());
        for (File file : experimentalSettingsFiles) {
            experimentalSettingsFilesModel.addElement(new TSDZ2_Configurator.FileContainer(file));
            if (lastSettingsFile == null) {
				lastSettingsFile = file;
			} else {
				if(file.lastModified()>lastSettingsFile.lastModified()){
					lastSettingsFile = file;
				}
			}
        }
        
        experimentalSettingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		experimentalSettingsList.setLayoutOrientation(JList.VERTICAL);

		experimentalSettingsList.setVisibleRowCount(-1); 
                
        expSet.setModel(experimentalSettingsFilesModel);
                
		provenSettingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		provenSettingsList.setLayoutOrientation(JList.VERTICAL);
		provenSettingsList.setVisibleRowCount(-1);

        provSet.setModel(provenSettingsFilesModel);
        jScrollPane2.setViewportView(provSet);

        expSet.setSelectedIndex(0);

        expSet.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
                try {
                    int selectedIndex = expSet.getSelectedIndex();
                    experimentalSettingsList.setSelectedIndex(selectedIndex);
					loadSettings(((FileContainer) experimentalSettingsList.getSelectedValue()).file);
					experimentalSettingsList.clearSelection();
				} catch (IOException ex) {
					Logger.getLogger(TSDZ2_Configurator.class.getName()).log(Level.SEVERE, null, ex);
				}
				experimentalSettingsList.clearSelection();

				//updateDependiencies(false);
			}
		});

         provSet.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
                try {
                    int selectedIndex = provSet.getSelectedIndex();
                    provenSettingsList.setSelectedIndex(selectedIndex);
					loadSettings(((FileContainer) provenSettingsList.getSelectedValue()).file);
					provenSettingsList.clearSelection();
				} catch (IOException ex) {
					Logger.getLogger(TSDZ2_Configurator.class.getName()).log(Level.SEVERE, null, ex);
				}
				provenSettingsList.clearSelection();
				//updateDependiencies(false);
			}
		});


          BTN_COMPILE.addActionListener(new ActionListener() {

          public void actionPerformed(ActionEvent arg0) {
          		PrintWriter iWriter = null;
                PrintWriter pWriter = null;

                File newFile = new File(experimentalSettingsDir + File.separator + new SimpleDateFormat("yyyyMMdd-HHmmssz").format(new Date()) + ".ini");
				try {
                    AddListItem(newFile);

					iWriter = new PrintWriter(new BufferedWriter(new FileWriter(newFile)));
					pWriter = new PrintWriter(new BufferedWriter(new FileWriter("src/config.h")));
                pWriter.println("/*\r\n"
                                + " * config.h\r\n"
                                + " *\r\n"
                                + " *  Automatically created by TSDS2 Parameter Configurator\r\n"
                                + " *  Author: stancecoke\r\n"
                                + " */\r\n"
                                + "\r\n"
                                + "#ifndef CONFIG_H_\r\n"
                                + "#define CONFIG_H_\r\n");
                String text_to_save;

                if (RB_MOTOR_36V.isSelected()) {
                    text_to_save = "#define MOTOR_TYPE 1";
                    pWriter.println(text_to_save);
                }
                iWriter.println(RB_MOTOR_36V.isSelected());

                if (RB_MOTOR_48V.isSelected()) {
                    text_to_save = "#define MOTOR_TYPE 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(RB_MOTOR_48V.isSelected());

                if (CB_TORQUE_CALIBRATION.isSelected()) {
                    text_to_save = "#define TORQUE_SENSOR_CALIBRATED 1";
                    pWriter.println(text_to_save);
                }
                else {
                text_to_save = "#define TORQUE_SENSOR_CALIBRATED 0";
                pWriter.println(text_to_save);
                }
                iWriter.println(CB_TORQUE_CALIBRATION.isSelected());

                text_to_save = "#define MOTOR_ACCELERATION  " + TF_MOTOR_ACC.getText();
                iWriter.println(TF_MOTOR_ACC.getText());
                pWriter.println(text_to_save);

                if (CB_ASS_WITHOUT_PED.isSelected()) {
                    text_to_save = "#define MOTOR_ASSISTANCE_WITHOUT_PEDAL_ROTATION 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define MOTOR_ASSISTANCE_WITHOUT_PEDAL_ROTATION 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_ASS_WITHOUT_PED.isSelected());

                text_to_save = "#define ASSISTANCE_WITHOUT_PEDAL_ROTATION_THRESHOLD " + TF_ASS_WITHOUT_PED_THRES.getText();
                iWriter.println(TF_ASS_WITHOUT_PED_THRES.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define PEDAL_TORQUE_PER_10_BIT_ADC_STEP_X100 " + TF_TORQ_PER_ADC_STEP.getText();
                iWriter.println(TF_TORQ_PER_ADC_STEP.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define PEDAL_TORQUE_ADC_MAX " + TF_TORQUE_ADC_MAX.getText();
                iWriter.println(TF_TORQUE_ADC_MAX.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define STARTUP_BOOST_TORQUE_FACTOR " + TF_BOOST_TORQUE_FACTOR.getText();
                iWriter.println(TF_BOOST_TORQUE_FACTOR.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define MOTOR_BLOCKED_COUNTER_THRESHOLD " + TF_MOTOR_BLOCK_TIME.getText();
                iWriter.println(TF_MOTOR_BLOCK_TIME.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define MOTOR_BLOCKED_BATTERY_CURRENT_THRESHOLD_X10 " + TF_MOTOR_BLOCK_CURR.getText();
                iWriter.println(TF_MOTOR_BLOCK_CURR.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define MOTOR_BLOCKED_ERPS_THRESHOLD " + TF_MOTOR_BLOCK_ERPS.getText();
                iWriter.println(TF_MOTOR_BLOCK_ERPS.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define STARTUP_BOOST_CADENCE_STEP " + TF_BOOST_CADENCE_STEP.getText();
                iWriter.println(TF_BOOST_CADENCE_STEP.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define BATTERY_CURRENT_MAX " + TF_BAT_CUR_MAX.getText();
                iWriter.println(TF_BAT_CUR_MAX.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define TARGET_MAX_BATTERY_POWER " + TF_BATT_POW_MAX.getText();
                iWriter.println(TF_BATT_POW_MAX.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define TARGET_MAX_BATTERY_CAPACITY " + TF_BATT_CAPACITY.getText();
                iWriter.println(TF_BATT_CAPACITY.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define BATTERY_CELLS_NUMBER " + TF_BATT_NUM_CELLS.getText();
                iWriter.println(TF_BATT_NUM_CELLS.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define MOTOR_DECELERATION " + TF_MOTOR_DEC.getText();
                iWriter.println(TF_MOTOR_DEC.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define BATTERY_LOW_VOLTAGE_CUT_OFF " + TF_BATT_VOLT_CUT_OFF.getText();
                iWriter.println(TF_BATT_VOLT_CUT_OFF.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define ACTUAL_BATTERY_VOLTAGE_PERCENT " + TF_BATT_VOLT_CAL.getText();
                iWriter.println(TF_BATT_VOLT_CAL.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define ACTUAL_BATTERY_CAPACITY_PERCENT " + TF_BATT_CAPACITY_CAL.getText();
                iWriter.println(TF_BATT_CAPACITY_CAL.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_OVERVOLT " + TF_BAT_CELL_OVER.getText();
                iWriter.println(TF_BAT_CELL_OVER.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_RESET_SOC_PERCENT " + TF_BAT_CELL_SOC.getText();
                iWriter.println(TF_BAT_CELL_SOC.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_FULL " + TF_BAT_CELL_FULL.getText();
                iWriter.println(TF_BAT_CELL_FULL.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_3_OF_4 " + TF_BAT_CELL_3_4.getText();
                iWriter.println(TF_BAT_CELL_3_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_2_OF_4 " + TF_BAT_CELL_2_4.getText();
                iWriter.println(TF_BAT_CELL_2_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_1_OF_4 " + TF_BAT_CELL_1_4.getText();
                iWriter.println(TF_BAT_CELL_1_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_5_OF_6 " + TF_BAT_CELL_5_6.getText();
                iWriter.println(TF_BAT_CELL_5_6.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_4_OF_6 " + TF_BAT_CELL_4_6.getText();
                iWriter.println(TF_BAT_CELL_4_6.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_3_OF_6 " + TF_BAT_CELL_3_6.getText();
                iWriter.println(TF_BAT_CELL_3_6.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_2_OF_6 " + TF_BAT_CELL_2_6.getText();
                iWriter.println(TF_BAT_CELL_2_6.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_1_OF_6 " + TF_BAT_CELL_1_6.getText();
                iWriter.println(TF_BAT_CELL_1_6.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LI_ION_CELL_VOLTS_EMPTY " + TF_BAT_CELL_EMPTY.getText();
                iWriter.println(TF_BAT_CELL_EMPTY.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define WHEEL_PERIMETER " + TF_WHEEL_CIRCUMF.getText();
                iWriter.println(TF_WHEEL_CIRCUMF.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define WHEEL_MAX_SPEED " + String.valueOf(intMaxSpeed);
                iWriter.println(String.valueOf(intMaxSpeed));
                pWriter.println(text_to_save);

                if (CB_LIGHTS.isSelected()) {
                    text_to_save = "#define ENABLE_LIGHTS 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_LIGHTS 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_LIGHTS.isSelected());

                if (CB_WALK_ASSIST_ENABLED.isSelected()) {
                    text_to_save = "#define ENABLE_WALK_ASSIST 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_WALK_ASSIST 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_WALK_ASSIST_ENABLED.isSelected());

                if ((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR)||(JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)) {
                    text_to_save = "#define ENABLE_BRAKE_SENSOR 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_BRAKE_SENSOR 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolBrakeSensor);

                if (JCB_OPTIONAL_ADC.getSelectedIndex() == DISABLED) {
                    text_to_save = "#define ENABLE_THROTTLE 0"+System.getProperty("line.separator")+"#define ENABLE_TEMPERATURE_LIMIT 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolOptionalAdcDisabled);

                if (JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE) {
                    text_to_save = "#define ENABLE_THROTTLE 1"+System.getProperty("line.separator")+"#define ENABLE_TEMPERATURE_LIMIT 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolOptionalAdcThrottle);

                 if (JCB_OPTIONAL_ADC.getSelectedIndex() == TEMPERATURE) {
                    text_to_save = "#define ENABLE_THROTTLE 0"+System.getProperty("line.separator")+"#define ENABLE_TEMPERATURE_LIMIT 1";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolOptionalAdcTemperature);

                if (CB_STREET_MODE_ON_START.isSelected()) {
                    text_to_save = "#define ENABLE_STREET_MODE_ON_STARTUP 1";
                    pWriter.println(text_to_save);
                }
                else {
                text_to_save = "#define ENABLE_STREET_MODE_ON_STARTUP 0";
                pWriter.println(text_to_save);
                }
                iWriter.println(CB_STREET_MODE_ON_START.isSelected());

                if (CB_SET_PARAM_ON_START.isSelected()) {
                    text_to_save = "#define ENABLE_SET_PARAMETER_ON_STARTUP 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_SET_PARAMETER_ON_STARTUP 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_SET_PARAM_ON_START.isSelected());

                if (CB_ODO_COMPENSATION.isSelected()) {
                    text_to_save = "#define ENABLE_ODOMETER_COMPENSATION 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_ODOMETER_COMPENSATION 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_ODO_COMPENSATION.isSelected());

                if (CB_STARTUP_BOOST_ON_START.isSelected()) {
                    text_to_save = "#define STARTUP_BOOST_ON_STARTUP 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define STARTUP_BOOST_ON_STARTUP 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_STARTUP_BOOST_ON_START.isSelected());

                if (CB_TOR_SENSOR_ADV.isSelected()) {
                    text_to_save = "#define TORQUE_SENSOR_ADV_ON_STARTUP 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define TORQUE_SENSOR_ADV_ON_STARTUP 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_TOR_SENSOR_ADV.isSelected());

                text_to_save = "#define LIGHTS_CONFIGURATION_ON_STARTUP " + TF_LIGHT_MODE_ON_START.getText();
                iWriter.println(TF_LIGHT_MODE_ON_START.getText());
                pWriter.println(text_to_save);

                if (JCB_ASSIST_MODE_ON_STARTUP.getSelectedIndex() == POWER) {
                    text_to_save = "#define RIDING_MODE_ON_STARTUP 1";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolAssistStartupPower);

                if (JCB_ASSIST_MODE_ON_STARTUP.getSelectedIndex() == TORQUE) {
                    text_to_save = "#define RIDING_MODE_ON_STARTUP 2";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolAssistStartupTorque);

                if (JCB_ASSIST_MODE_ON_STARTUP.getSelectedIndex() == CADENCE) {
                    text_to_save = "#define RIDING_MODE_ON_STARTUP 3";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolAssistStartupCadence);

                if (JCB_ASSIST_MODE_ON_STARTUP.getSelectedIndex() == EMTB) {
                    text_to_save = "#define RIDING_MODE_ON_STARTUP 4";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolAssistStartupEmtb);

                text_to_save = "#define LIGHTS_CONFIGURATION_1 " + TF_LIGHT_MODE_1.getText();
                iWriter.println(TF_LIGHT_MODE_1.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LIGHTS_CONFIGURATION_2 " + TF_LIGHT_MODE_2.getText();
                iWriter.println(TF_LIGHT_MODE_2.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define LIGHTS_CONFIGURATION_3 " + TF_LIGHT_MODE_3.getText();
                iWriter.println(TF_LIGHT_MODE_3.getText());
                pWriter.println(text_to_save);

                if (CB_STREET_POWER_LIM.isSelected()) {
                    text_to_save = "#define STREET_MODE_POWER_LIMIT_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define STREET_MODE_POWER_LIMIT_ENABLED 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_STREET_POWER_LIM.isSelected());

                text_to_save = "#define STREET_MODE_POWER_LIMIT " + TF_STREET_POWER_LIM.getText();
                iWriter.println(TF_STREET_POWER_LIM.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define STREET_MODE_SPEED_LIMIT " + String.valueOf(intStreetSpeed);
                iWriter.println(String.valueOf(intStreetSpeed));
                pWriter.println(text_to_save);
                
                // Not used
                if ((JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE)&&(JCB_THROTTLE_MODE_STREET.getSelectedIndex() == UNCONDITIONAL)) {
                    text_to_save = "#define STREET_MODE_THROTTLE_ENABLED 1";
                    pWriter.println(text_to_save);
                    iWriter.println(true);
                }
                else {
                    text_to_save = "#define STREET_MODE_THROTTLE_ENABLED 0";
                    pWriter.println(text_to_save);
                    iWriter.println(false);
                }

                if (CB_STREET_CRUISE.isSelected()) {
                    text_to_save = "#define STREET_MODE_CRUISE_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define STREET_MODE_CRUISE_ENABLED 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_STREET_CRUISE.isSelected());

                text_to_save = "#define ADC_THROTTLE_MIN_VALUE " + TF_ADC_THROTTLE_MIN.getText();
                iWriter.println(TF_ADC_THROTTLE_MIN.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define ADC_THROTTLE_MAX_VALUE " + TF_ADC_THROTTLE_MAX.getText();
                iWriter.println(TF_ADC_THROTTLE_MAX.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define MOTOR_TEMPERATURE_MIN_VALUE_LIMIT " + TF_TEMP_MIN_LIM.getText();
                iWriter.println(TF_TEMP_MIN_LIM.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define MOTOR_TEMPERATURE_MAX_VALUE_LIMIT " + TF_TEMP_MAX_LIM.getText();
                iWriter.println(TF_TEMP_MAX_LIM.getText());
                pWriter.println(text_to_save);

                if (CB_TEMP_ERR_MIN_LIM.isSelected()) {
                    text_to_save = "#define ENABLE_TEMPERATURE_ERROR_MIN_LIMIT 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_TEMPERATURE_ERROR_MIN_LIMIT 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_TEMP_ERR_MIN_LIM.isSelected());

                if (JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD6) {
                    text_to_save = "#define ENABLE_VLCD6 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_VLCD6 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolDisplayTypeVLCD6);

                if (JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD5) {
                    text_to_save = "#define ENABLE_VLCD5 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_VLCD5 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolDisplayTypeVLCD5);

                if (JCB_DISPLAY_TYPE.getSelectedIndex() == XH18) {
                    text_to_save = "#define ENABLE_XH18 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_XH18 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolDisplayTypeXH18);

                if (RB_DISPLAY_WORK_ON.isSelected()) {
                    text_to_save = "#define ENABLE_DISPLAY_WORKING_FLAG 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_DISPLAY_WORKING_FLAG 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(RB_DISPLAY_WORK_ON.isSelected());

                if (RB_DISPLAY_ALWAY_ON.isSelected()) {
                    text_to_save = "#define ENABLE_DISPLAY_ALWAYS_ON 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_DISPLAY_ALWAYS_ON 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(RB_DISPLAY_ALWAY_ON.isSelected());

                if (CB_MAX_SPEED_DISPLAY.isSelected()) {
                    text_to_save = "#define ENABLE_WHEEL_MAX_SPEED_FROM_DISPLAY 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_WHEEL_MAX_SPEED_FROM_DISPLAY 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_MAX_SPEED_DISPLAY.isSelected());

                text_to_save = "#define DELAY_MENU_ON " + TF_DELAY_MENU.getText();
                iWriter.println(TF_DELAY_MENU.getText());
                pWriter.println(text_to_save);

                if (JCB_BRAKE_FEATURE.getSelectedIndex() == COASTER_BRAKE) {
                    text_to_save = "#define COASTER_BRAKE_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define COASTER_BRAKE_ENABLED 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolCoasterBrake);

                text_to_save = "#define COASTER_BRAKE_TORQUE_THRESHOLD " + TF_COASTER_BRAKE_THRESHOLD.getText();
                iWriter.println(TF_COASTER_BRAKE_THRESHOLD.getText());
                pWriter.println(text_to_save);

                if (CB_AUTO_DISPLAY_DATA.isSelected()) {
                    text_to_save = "#define ENABLE_AUTO_DATA_DISPLAY 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLE_AUTO_DATA_DISPLAY 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_AUTO_DISPLAY_DATA.isSelected());

                if (CB_STARTUP_ASSIST_ENABLED.isSelected()) {
                    text_to_save = "#define STARTUP_ASSIST_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define STARTUP_ASSIST_ENABLED 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_STARTUP_ASSIST_ENABLED.isSelected());

                text_to_save = "#define DELAY_DISPLAY_DATA_1 " + TF_DELAY_DATA_1.getText();
                iWriter.println(TF_DELAY_DATA_1.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DELAY_DISPLAY_DATA_2 " + TF_DELAY_DATA_2.getText();
                iWriter.println(TF_DELAY_DATA_2.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DELAY_DISPLAY_DATA_3 " + TF_DELAY_DATA_3.getText();
                iWriter.println(TF_DELAY_DATA_3.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DELAY_DISPLAY_DATA_4 " + TF_DELAY_DATA_4.getText();
                iWriter.println(TF_DELAY_DATA_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DELAY_DISPLAY_DATA_5 " + TF_DELAY_DATA_5.getText();
                iWriter.println(TF_DELAY_DATA_5.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DELAY_DISPLAY_DATA_6 " + TF_DELAY_DATA_6.getText();
                iWriter.println(TF_DELAY_DATA_6.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DISPLAY_DATA_1 " + TF_DATA_1.getText();
                iWriter.println(TF_DATA_1.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DISPLAY_DATA_2 " + TF_DATA_2.getText();
                iWriter.println(TF_DATA_2.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DISPLAY_DATA_3 " + TF_DATA_3.getText();
                iWriter.println(TF_DATA_3.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DISPLAY_DATA_4 " + TF_DATA_4.getText();
                iWriter.println(TF_DATA_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DISPLAY_DATA_5 " + TF_DATA_5.getText();
                iWriter.println(TF_DATA_5.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define DISPLAY_DATA_6 " + TF_DATA_6.getText();
                iWriter.println(TF_DATA_6.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define POWER_ASSIST_LEVEL_1 " + TF_POWER_ASS_1.getText();
                iWriter.println(TF_POWER_ASS_1.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define POWER_ASSIST_LEVEL_2 " + TF_POWER_ASS_2.getText();
                iWriter.println(TF_POWER_ASS_2.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define POWER_ASSIST_LEVEL_3 " + TF_POWER_ASS_3.getText();
                iWriter.println(TF_POWER_ASS_3.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define POWER_ASSIST_LEVEL_4 " + TF_POWER_ASS_4.getText();
                iWriter.println(TF_POWER_ASS_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define TORQUE_ASSIST_LEVEL_1 " + TF_TORQUE_ASS_1.getText();
                iWriter.println(TF_TORQUE_ASS_1.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define TORQUE_ASSIST_LEVEL_2 " + TF_TORQUE_ASS_2.getText();
                iWriter.println(TF_TORQUE_ASS_2.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define TORQUE_ASSIST_LEVEL_3 " + TF_TORQUE_ASS_3.getText();
                iWriter.println(TF_TORQUE_ASS_3.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define TORQUE_ASSIST_LEVEL_4 " + TF_TORQUE_ASS_4.getText();
                iWriter.println(TF_TORQUE_ASS_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define CADENCE_ASSIST_LEVEL_1 " + TF_CADENCE_ASS_1.getText();
                iWriter.println(TF_CADENCE_ASS_1.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define CADENCE_ASSIST_LEVEL_2 " + TF_CADENCE_ASS_2.getText();
                iWriter.println(TF_CADENCE_ASS_2.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define CADENCE_ASSIST_LEVEL_3 " + TF_CADENCE_ASS_3.getText();
                iWriter.println(TF_CADENCE_ASS_3.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define CADENCE_ASSIST_LEVEL_4 " + TF_CADENCE_ASS_4.getText();
                iWriter.println(TF_CADENCE_ASS_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define EMTB_ASSIST_LEVEL_1 " + TF_EMTB_ASS_1.getText();
                iWriter.println(TF_EMTB_ASS_1.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define EMTB_ASSIST_LEVEL_2 " + TF_EMTB_ASS_2.getText();
                iWriter.println(TF_EMTB_ASS_2.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define EMTB_ASSIST_LEVEL_3 " + TF_EMTB_ASS_3.getText();
                iWriter.println(TF_EMTB_ASS_3.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define EMTB_ASSIST_LEVEL_4 " + TF_EMTB_ASS_4.getText();
                iWriter.println(TF_EMTB_ASS_4.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define WALK_ASSIST_LEVEL_1 " + String.valueOf(intWalkSpeed1);
                iWriter.println(String.valueOf(intWalkSpeed1));
                pWriter.println(text_to_save);

                text_to_save = "#define WALK_ASSIST_LEVEL_2 " + String.valueOf(intWalkSpeed2);
                iWriter.println(String.valueOf(intWalkSpeed2));
                pWriter.println(text_to_save);

                text_to_save = "#define WALK_ASSIST_LEVEL_3 " + String.valueOf(intWalkSpeed3);
                iWriter.println(String.valueOf(intWalkSpeed3));
                pWriter.println(text_to_save);

                text_to_save = "#define WALK_ASSIST_LEVEL_4 " + String.valueOf(intWalkSpeed4);
                iWriter.println(String.valueOf(intWalkSpeed4));
                pWriter.println(text_to_save);
                
                text_to_save = "#define WALK_ASSIST_THRESHOLD_SPEED_X10 " + String.valueOf(intWalkSpeedLimit);
                iWriter.println(String.valueOf(intWalkSpeedLimit));
                pWriter.println(text_to_save);

                if (CB_WALK_TIME_ENA.isSelected()) {
                    text_to_save = "#define WALK_ASSIST_DEBOUNCE_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define WALK_ASSIST_DEBOUNCE_ENABLED 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_WALK_TIME_ENA.isSelected());

                text_to_save = "#define WALK_ASSIST_DEBOUNCE_TIME " + TF_WALK_ASS_TIME.getText();
                iWriter.println(TF_WALK_ASS_TIME.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define CRUISE_TARGET_SPEED_LEVEL_1 " + String.valueOf(intCruiseSpeed1);
                iWriter.println(String.valueOf(intCruiseSpeed1));
                pWriter.println(text_to_save);

                text_to_save = "#define CRUISE_TARGET_SPEED_LEVEL_2 " + String.valueOf(intCruiseSpeed2);
                iWriter.println(String.valueOf(intCruiseSpeed2));
                pWriter.println(text_to_save);

                text_to_save = "#define CRUISE_TARGET_SPEED_LEVEL_3 " + String.valueOf(intCruiseSpeed3);
                iWriter.println(String.valueOf(intCruiseSpeed3));
                pWriter.println(text_to_save);

                text_to_save = "#define CRUISE_TARGET_SPEED_LEVEL_4 " + String.valueOf(intCruiseSpeed4);
                iWriter.println(String.valueOf(intCruiseSpeed4));
                pWriter.println(text_to_save);

                if (CB_CRUISE_WHITOUT_PEDALING.isSelected()) {
                    text_to_save = "#define CRUISE_MODE_WALK_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define CRUISE_MODE_WALK_ENABLED 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_CRUISE_WHITOUT_PEDALING.isSelected());

                text_to_save = "#define CRUISE_THRESHOLD_SPEED " + String.valueOf(intCruiseSpeed);
                iWriter.println(String.valueOf(intCruiseSpeed));
                pWriter.println(text_to_save);

                text_to_save = "#define PEDAL_TORQUE_ADC_OFFSET " + TF_TORQ_ADC_OFFSET.getText();
                iWriter.println(TF_TORQ_ADC_OFFSET.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define AUTO_DATA_NUMBER_DISPLAY " + TF_NUM_DATA_AUTO_DISPLAY.getText();
                iWriter.println(TF_NUM_DATA_AUTO_DISPLAY.getText());
                pWriter.println(text_to_save);

                if ((JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
                    text_to_save = "#define UNITS_TYPE 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolUnitsKilometers);
                
                if (JCB_UNITS_TYPE.getSelectedIndex() == MILES) {
                    if (boolDisplayTypeVLCD6) {
                        text_to_save = "#define UNITS_TYPE 0";
                        pWriter.println(text_to_save);
                    }
                    else {
                        text_to_save = "#define UNITS_TYPE 1";
                        pWriter.println(text_to_save);
                    }
                }
                iWriter.println(boolUnitsMiles);

                text_to_save = "#define ASSIST_THROTTLE_MIN_VALUE " + TF_ASSIST_THROTTLE_MIN.getText();
                iWriter.println(TF_ASSIST_THROTTLE_MIN.getText());
                pWriter.println(text_to_save);

                text_to_save = "#define ASSIST_THROTTLE_MAX_VALUE " + TF_ASSIST_THROTTLE_MAX.getText();
                iWriter.println(TF_ASSIST_THROTTLE_MAX.getText());
                pWriter.println(text_to_save);

                if (CB_STREET_WALK.isSelected()) {
                    text_to_save = "#define STREET_MODE_WALK_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define STREET_MODE_WALK_ENABLED 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_STREET_WALK.isSelected());

                if (JCB_ASSIST_MODE_ON_STARTUP.getSelectedIndex() == HYBRID) {
                    text_to_save = "#define RIDING_MODE_ON_STARTUP 5";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolAssistStartupHybrid);

                if (JCB_DATA_ON_STARTUP.getSelectedIndex() == NONE) {
                    text_to_save = "#define DATA_DISPLAY_ON_STARTUP 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolStartupNone);

                if (JCB_DATA_ON_STARTUP.getSelectedIndex() == SOC) {
                    text_to_save = "#define DATA_DISPLAY_ON_STARTUP 1";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolStartupSoc);

                if (JCB_DATA_ON_STARTUP.getSelectedIndex() == VOLTS) {
                    text_to_save = "#define DATA_DISPLAY_ON_STARTUP 2";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolStartupVolts);

                if (CB_FIELD_WEAKENING_ENABLED.isSelected()) {
                    text_to_save = "#define FIELD_WEAKENING_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                text_to_save = "#define FIELD_WEAKENING_ENABLED 0";
                pWriter.println(text_to_save);
                }
                iWriter.println(CB_FIELD_WEAKENING_ENABLED.isSelected());

                text_to_save = "#define PEDAL_TORQUE_ADC_OFFSET_ADJ " + String.valueOf(intTorqueOffsetAdj);
                iWriter.println(String.valueOf(intTorqueOffsetAdj));
                pWriter.println(text_to_save);

                text_to_save = "#define PEDAL_TORQUE_ADC_RANGE_ADJ " + String.valueOf(intTorqueRangeAdj);
                iWriter.println(String.valueOf(intTorqueRangeAdj));
                pWriter.println(text_to_save);

                text_to_save = "#define PEDAL_TORQUE_ADC_ANGLE_ADJ " + String.valueOf(intAdcPedalTorqueAngleAdjArray[intTorqueAngleAdj]);
                iWriter.println(String.valueOf(intTorqueAngleAdj));
                pWriter.println(text_to_save);

                text_to_save = "#define PEDAL_TORQUE_PER_10_BIT_ADC_STEP_ADV_X100 " + TF_TORQ_PER_ADC_STEP_ADV.getText();
                iWriter.println(TF_TORQ_PER_ADC_STEP_ADV.getText());
                pWriter.println(text_to_save);

                if (JCB_SOC_CALC.getSelectedIndex() == AUTO) {
                    text_to_save = "#define SOC_PERCENT_CALC 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolSocAuto);

                if (JCB_SOC_CALC.getSelectedIndex() == WH) {
                    text_to_save = "#define SOC_PERCENT_CALC 1";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolSocWh);

                if (JCB_SOC_CALC.getSelectedIndex() == VOLTS) {
                    text_to_save = "#define SOC_PERCENT_CALC 2";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolSocVolts);

                iWriter.println(CB_ADC_STEP_ESTIM.isSelected());

                if (RB_BOOST_AT_ZERO_CADENCE.isSelected()) {
                    text_to_save = "#define STARTUP_BOOST_AT_ZERO 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(RB_BOOST_AT_ZERO_CADENCE.isSelected());

                if (RB_BOOST_AT_ZERO_SPEED.isSelected()) {
                    text_to_save = "#define STARTUP_BOOST_AT_ZERO 1";
                    pWriter.println(text_to_save);
                }
                iWriter.println(RB_BOOST_AT_ZERO_SPEED.isSelected());

                if (JCB_DISPLAY_TYPE.getSelectedIndex() == C850) {
                    text_to_save = "#define ENABLEC850 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ENABLEC850 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolDisplayType850C);
                
                // not used
                if ((JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE)&&(JCB_THROTTLE_MODE_STREET.getSelectedIndex() == PEDALING)) {
                    text_to_save = "#define STREET_MODE_THROTTLE_LEGAL 1";
                    pWriter.println(text_to_save);
                    iWriter.println(true);
                }
                else {
                    text_to_save = "#define STREET_MODE_THROTTLE_LEGAL 0";
                    pWriter.println(text_to_save);
                    iWriter.println(false);
                }
                
                if (JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH) {
                    text_to_save = "#define BRAKE_TEMPERATURE_SWITCH 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define BRAKE_TEMPERATURE_SWITCH 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolTemperatureSwitch);
                
                if (RB_eMTB_POWER.isSelected()) {
                    text_to_save = "#define eMTB_BASED_ON_POWER 1";
                    pWriter.println(text_to_save);
                }
                iWriter.println(RB_eMTB_POWER.isSelected());
                
                if (RB_eMTB_TORQUE.isSelected()) {
                    text_to_save = "#define eMTB_BASED_ON_POWER 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(RB_eMTB_TORQUE.isSelected());
                
                if (CB_SMOOTH_START_ENABLED.isSelected()) {
                    text_to_save = "#define SMOOTH_START_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                text_to_save = "#define SMOOTH_START_ENABLED 0";
                pWriter.println(text_to_save);
                }
                iWriter.println(CB_SMOOTH_START_ENABLED.isSelected());
                
                text_to_save = "#define SMOOTH_START_SET_PERCENT " + TF_SMOOTH_START_RAMP.getText();
                iWriter.println(TF_SMOOTH_START_RAMP.getText());
                pWriter.println(text_to_save);
                
                if (JCB_TEMP_SENSOR_TYPE.getSelectedIndex() == LM35) {
                    text_to_save = "#define TEMPERATURE_SENSOR_TYPE 0";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define TEMPERATURE_SENSOR_TYPE 1";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolTemperatureSensorType);
                
                if (CB_CRUISE_ENABLED.isSelected()) {
                    text_to_save = "#define CRUISE_MODE_ENABLED 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define CRUISE_MODE_ENABLED 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(CB_CRUISE_ENABLED.isSelected());
                
                text_to_save = "#define THROTTLE_MODE " + String.valueOf(JCB_THROTTLE_MODE.getSelectedIndex());
                iWriter.println(String.valueOf(JCB_THROTTLE_MODE.getSelectedIndex()));
                pWriter.println(text_to_save);
                
                text_to_save = "#define STREET_MODE_THROTTLE_MODE " + String.valueOf(JCB_THROTTLE_MODE_STREET.getSelectedIndex());
                iWriter.println(String.valueOf(JCB_THROTTLE_MODE_STREET.getSelectedIndex()));
                pWriter.println(text_to_save);
                
                text_to_save = "#define ASSIST_LEVEL_1_OF_5_PERCENT " + TF_ASS_LEVELS_1_OF_5.getText();
                iWriter.println(TF_ASS_LEVELS_1_OF_5.getText());
                pWriter.println(text_to_save);
                
                if ((JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)||((JCB_UNITS_TYPE.getSelectedIndex() == MILES)&&(boolDisplayTypeVLCD6))) {
                    text_to_save = "#define ALTERNATIVE_MILES 1";
                    pWriter.println(text_to_save);
                }
                else {
                    text_to_save = "#define ALTERNATIVE_MILES 0";
                    pWriter.println(text_to_save);
                }
                iWriter.println(boolAlternativeMiles);
                
                pWriter.println("\r\n#endif /* CONFIG_H_ */");

                iWriter.close();
                
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                } finally {
                    if (pWriter != null) {
                        pWriter.flush();
                        pWriter.close();
                    }
                }
            
                try {
                    String backup_name = newFile.getName();
                    backup_name = backup_name.substring(0, backup_name.lastIndexOf('.')); //remove ini extension

                    // Detect OS
                    OSType os = getOperatingSystem();
                    Process process;
                    switch(os) {
                        case Windows:
                            process = Runtime.getRuntime().exec("cmd /c start compile_and_flash_20 " + backup_name);
                            break;
                        case MacOS:
                        case Linux:
                            process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "xterm -fa fixed -fs 10 -hold -e 'sh compile_and_flash_20.sh " + backup_name + "'" });
                            break;
                        case Other:
                            default:
                            JOptionPane.showMessageDialog(null, " Unknown OS.\n Please run:\ncd src && make && make clear_eeprom && make flash\nto compile and flash your TSDZ2.");
                            break;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace(System.err);
                }
            }
        });

        if (lastSettingsFile != null) {

            try {
                loadSettings(lastSettingsFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, " " + ex);
            }
        
            provenSettingsList.clearSelection();
            experimentalSettingsList.clearSelection();
            //updateDependiencies(false);
        }
    }

    /**
     * This method detect the current operating system used
     */
    public enum OSType {
        Windows, MacOS, Linux, Other
    };
    private OSType getOperatingSystem() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.contains("mac")) || (OS.contains("darwin"))) return OSType.MacOS;
        else if (OS.contains("win")) return OSType.Windows;
        else if (OS.contains("nux")) return OSType.Linux;
        return OSType.Other;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jRadioButton1 = new javax.swing.JRadioButton();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        buttonGroup9 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        Label_Parameter1 = new javax.swing.JLabel();
        RB_MOTOR_36V = new javax.swing.JRadioButton();
        CB_ASS_WITHOUT_PED = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        TF_BOOST_TORQUE_FACTOR = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        TF_MOTOR_ACC = new javax.swing.JTextField();
        TF_ASS_WITHOUT_PED_THRES = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        TF_BOOST_CADENCE_STEP = new javax.swing.JTextField();
        RB_MOTOR_48V = new javax.swing.JRadioButton();
        jLabelMotorFastStop = new javax.swing.JLabel();
        TF_MOTOR_DEC = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        RB_BOOST_AT_ZERO_CADENCE = new javax.swing.JRadioButton();
        RB_BOOST_AT_ZERO_SPEED = new javax.swing.JRadioButton();
        CB_STARTUP_BOOST_ON_START = new javax.swing.JCheckBox();
        CB_FIELD_WEAKENING_ENABLED = new javax.swing.JCheckBox();
        CB_STARTUP_ASSIST_ENABLED = new javax.swing.JCheckBox();
        jLabel88 = new javax.swing.JLabel();
        CB_SMOOTH_START_ENABLED = new javax.swing.JCheckBox();
        jLabel77 = new javax.swing.JLabel();
        TF_SMOOTH_START_RAMP = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        TF_BATT_CAPACITY = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        TF_BAT_CUR_MAX = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        TF_BATT_POW_MAX = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        TF_BATT_NUM_CELLS = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        TF_BATT_VOLT_CAL = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        TF_BATT_CAPACITY_CAL = new javax.swing.JTextField();
        TF_BATT_VOLT_CUT_OFF = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        Label_Parameter3 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        Label_Parameter2 = new javax.swing.JLabel();
        RB_DISPLAY_WORK_ON = new javax.swing.JRadioButton();
        RB_DISPLAY_ALWAY_ON = new javax.swing.JRadioButton();
        Label_Parameter5 = new javax.swing.JLabel();
        JCB_DISPLAY_TYPE = new javax.swing.JComboBox<>();
        CB_ODO_COMPENSATION = new javax.swing.JCheckBox();
        CB_SET_PARAM_ON_START = new javax.swing.JCheckBox();
        CB_AUTO_DISPLAY_DATA = new javax.swing.JCheckBox();
        JCB_UNITS_TYPE = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        CB_TOR_SENSOR_ADV = new javax.swing.JCheckBox();
        CB_TORQUE_CALIBRATION = new javax.swing.JCheckBox();
        TF_TORQ_ADC_OFFSET_ADJ = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        TF_TORQ_ADC_RANGE_ADJ = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        TF_TORQ_PER_ADC_STEP = new javax.swing.JTextField();
        TF_TORQUE_ADC_MAX = new javax.swing.JTextField();
        jLabel_TORQ_ADC_MAX = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel_TORQ_ADC_OFFSET = new javax.swing.JLabel();
        CB_ADC_STEP_ESTIM = new javax.swing.JCheckBox();
        TF_TORQ_ADC_OFFSET = new javax.swing.JTextField();
        TF_TORQ_PER_ADC_STEP_ADV = new javax.swing.JTextField();
        TF_TORQ_ADC_ANGLE_ADJ = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        TF_WHEEL_CIRCUMF = new javax.swing.JTextField();
        jLabelMaxSpeed = new javax.swing.JLabel();
        TF_MAX_SPEED = new javax.swing.JTextField();
        CB_MAX_SPEED_DISPLAY = new javax.swing.JCheckBox();
        jLabel79 = new javax.swing.JLabel();
        JCB_ASSIST_MODE_ON_STARTUP = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        TF_POWER_ASS_1 = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        TF_POWER_ASS_2 = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        TF_POWER_ASS_3 = new javax.swing.JTextField();
        TF_POWER_ASS_4 = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        TF_TORQUE_ASS_1 = new javax.swing.JTextField();
        jLabel45 = new javax.swing.JLabel();
        TF_TORQUE_ASS_2 = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        TF_TORQUE_ASS_3 = new javax.swing.JTextField();
        TF_TORQUE_ASS_4 = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        TF_CADENCE_ASS_1 = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        TF_CADENCE_ASS_2 = new javax.swing.JTextField();
        jLabel51 = new javax.swing.JLabel();
        TF_CADENCE_ASS_3 = new javax.swing.JTextField();
        TF_CADENCE_ASS_4 = new javax.swing.JTextField();
        jLabel52 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        TF_EMTB_ASS_1 = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        TF_EMTB_ASS_2 = new javax.swing.JTextField();
        jLabel56 = new javax.swing.JLabel();
        TF_EMTB_ASS_3 = new javax.swing.JTextField();
        TF_EMTB_ASS_4 = new javax.swing.JTextField();
        jLabel57 = new javax.swing.JLabel();
        RB_eMTB_POWER = new javax.swing.JRadioButton();
        jLabel78 = new javax.swing.JLabel();
        RB_eMTB_TORQUE = new javax.swing.JRadioButton();
        jPanel15 = new javax.swing.JPanel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        TF_WALK_ASS_SPEED_1 = new javax.swing.JTextField();
        jLabel64 = new javax.swing.JLabel();
        TF_WALK_ASS_SPEED_2 = new javax.swing.JTextField();
        jLabel65 = new javax.swing.JLabel();
        TF_WALK_ASS_SPEED_3 = new javax.swing.JTextField();
        TF_WALK_ASS_SPEED_4 = new javax.swing.JTextField();
        jLabel66 = new javax.swing.JLabel();
        jLabelWalkSpeed = new javax.swing.JLabel();
        TF_WALK_ASS_SPEED_LIMIT = new javax.swing.JTextField();
        TF_WALK_ASS_TIME = new javax.swing.JTextField();
        jLabel68 = new javax.swing.JLabel();
        CB_WALK_TIME_ENA = new javax.swing.JCheckBox();
        jLabelWalkSpeedUnits = new javax.swing.JLabel();
        CB_WALK_ASSIST_ENABLED = new javax.swing.JCheckBox();
        jPanel16 = new javax.swing.JPanel();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        TF_CRUISE_ASS_1 = new javax.swing.JTextField();
        jLabel71 = new javax.swing.JLabel();
        TF_CRUISE_ASS_2 = new javax.swing.JTextField();
        jLabel72 = new javax.swing.JLabel();
        TF_CRUISE_ASS_3 = new javax.swing.JTextField();
        TF_CRUISE_ASS_4 = new javax.swing.JTextField();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        TF_CRUISE_SPEED_ENA = new javax.swing.JTextField();
        CB_CRUISE_WHITOUT_PEDALING = new javax.swing.JCheckBox();
        jLabelCruiseSpeedUnits = new javax.swing.JLabel();
        CB_CRUISE_ENABLED = new javax.swing.JCheckBox();
        jPanel17 = new javax.swing.JPanel();
        jLabel76 = new javax.swing.JLabel();
        jLabelLights0 = new javax.swing.JLabel();
        TF_LIGHT_MODE_ON_START = new javax.swing.JTextField();
        jLabelLights1 = new javax.swing.JLabel();
        TF_LIGHT_MODE_1 = new javax.swing.JTextField();
        jLabelLights2 = new javax.swing.JLabel();
        TF_LIGHT_MODE_2 = new javax.swing.JTextField();
        TF_LIGHT_MODE_3 = new javax.swing.JTextField();
        jLabelLights3 = new javax.swing.JLabel();
        CB_LIGHTS = new javax.swing.JCheckBox();
        jLabel67 = new javax.swing.JLabel();
        TF_ASS_LEVELS_1_OF_5 = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabelStreetSpeed = new javax.swing.JLabel();
        TF_STREET_SPEED_LIM = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        TF_STREET_POWER_LIM = new javax.swing.JTextField();
        CB_STREET_POWER_LIM = new javax.swing.JCheckBox();
        CB_STREET_CRUISE = new javax.swing.JCheckBox();
        CB_STREET_WALK = new javax.swing.JCheckBox();
        CB_STREET_MODE_ON_START = new javax.swing.JCheckBox();
        JCB_THROTTLE_MODE_STREET = new javax.swing.JComboBox<>();
        jLabel80 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        TF_BAT_CELL_FULL = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        TF_BAT_CELL_OVER = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        TF_BAT_CELL_SOC = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        TF_BAT_CELL_3_4 = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        TF_BAT_CELL_2_4 = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        TF_BAT_CELL_1_4 = new javax.swing.JTextField();
        jLabel75 = new javax.swing.JLabel();
        TF_BAT_CELL_5_6 = new javax.swing.JTextField();
        jLabel81 = new javax.swing.JLabel();
        TF_BAT_CELL_4_6 = new javax.swing.JTextField();
        jLabel82 = new javax.swing.JLabel();
        TF_BAT_CELL_3_6 = new javax.swing.JTextField();
        TF_BAT_CELL_2_6 = new javax.swing.JTextField();
        jLabel83 = new javax.swing.JLabel();
        TF_BAT_CELL_1_6 = new javax.swing.JTextField();
        jLabel84 = new javax.swing.JLabel();
        TF_BAT_CELL_EMPTY = new javax.swing.JTextField();
        jLabel85 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jLabel86 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        TF_DELAY_DATA_1 = new javax.swing.JTextField();
        jLabelData1 = new javax.swing.JLabel();
        TF_DATA_1 = new javax.swing.JTextField();
        TF_DATA_2 = new javax.swing.JTextField();
        jLabelData2 = new javax.swing.JLabel();
        TF_DATA_3 = new javax.swing.JTextField();
        jLabelData3 = new javax.swing.JLabel();
        jLabelData4 = new javax.swing.JLabel();
        TF_DATA_4 = new javax.swing.JTextField();
        TF_DATA_5 = new javax.swing.JTextField();
        jLabelData5 = new javax.swing.JLabel();
        jLabelData6 = new javax.swing.JLabel();
        TF_DATA_6 = new javax.swing.JTextField();
        jLabel96 = new javax.swing.JLabel();
        TF_DELAY_DATA_2 = new javax.swing.JTextField();
        jLabel97 = new javax.swing.JLabel();
        TF_DELAY_DATA_3 = new javax.swing.JTextField();
        jLabel98 = new javax.swing.JLabel();
        TF_DELAY_DATA_4 = new javax.swing.JTextField();
        jLabel99 = new javax.swing.JLabel();
        TF_DELAY_DATA_5 = new javax.swing.JTextField();
        jLabel100 = new javax.swing.JLabel();
        TF_DELAY_DATA_6 = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jLabel101 = new javax.swing.JLabel();
        jLabel102 = new javax.swing.JLabel();
        TF_ADC_THROTTLE_MIN = new javax.swing.JTextField();
        TF_ADC_THROTTLE_MAX = new javax.swing.JTextField();
        CB_TEMP_ERR_MIN_LIM = new javax.swing.JCheckBox();
        jLabel104 = new javax.swing.JLabel();
        TF_TEMP_MIN_LIM = new javax.swing.JTextField();
        jLabel105 = new javax.swing.JLabel();
        TF_TEMP_MAX_LIM = new javax.swing.JTextField();
        TF_DELAY_MENU = new javax.swing.JTextField();
        jLabel87 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
        TF_NUM_DATA_AUTO_DISPLAY = new javax.swing.JTextField();
        jLabel109 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jLabelCoasterBrakeThreshld = new javax.swing.JLabel();
        TF_COASTER_BRAKE_THRESHOLD = new javax.swing.JTextField();
        jLabel94 = new javax.swing.JLabel();
        JCB_DATA_ON_STARTUP = new javax.swing.JComboBox<>();
        JCB_SOC_CALC = new javax.swing.JComboBox<>();
        jLabel95 = new javax.swing.JLabel();
        JCB_BRAKE_FEATURE = new javax.swing.JComboBox<>();
        jLabel30 = new javax.swing.JLabel();
        JCB_OPTIONAL_ADC = new javax.swing.JComboBox<>();
        jLabel106 = new javax.swing.JLabel();
        JCB_THROTTLE_MODE = new javax.swing.JComboBox<>();
        jLabel107 = new javax.swing.JLabel();
        JCB_TEMP_SENSOR_TYPE = new javax.swing.JComboBox<>();
        TF_MOTOR_BLOCK_ERPS = new javax.swing.JTextField();
        TF_MOTOR_BLOCK_CURR = new javax.swing.JTextField();
        jLabelMOTOR_BLOCK_CURR = new javax.swing.JLabel();
        jLabelMOTOR_BLOCK_ERPS = new javax.swing.JLabel();
        jLabelTF_MOTOR_BLOCK_TIME = new javax.swing.JLabel();
        TF_MOTOR_BLOCK_TIME = new javax.swing.JTextField();
        jLabelTHROTTLE_ASSIST_MIN = new javax.swing.JLabel();
        TF_ASSIST_THROTTLE_MIN = new javax.swing.JTextField();
        jLabelTHROTTLE_ASSIST_MAX = new javax.swing.JLabel();
        TF_ASSIST_THROTTLE_MAX = new javax.swing.JTextField();
        label1 = new java.awt.Label();
        jScrollPane1 = new javax.swing.JScrollPane();
        expSet = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        provSet = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        BTN_COMPILE = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        LB_LAST_COMMIT = new javax.swing.JLabel();
        BTN_COPY_FILE_INI = new javax.swing.JButton();

        jRadioButton1.setText("jRadioButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TSDZ2 Parameter Configurator 5.0 for Open Source Firmware v20.1C.6");
        setResizable(false);
        setSize(new java.awt.Dimension(1192, 608));

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(894, 513));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setText("Motor settings");

        Label_Parameter1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        Label_Parameter1.setForeground(new java.awt.Color(255, 0, 0));
        Label_Parameter1.setText("Motor type");

        buttonGroup1.add(RB_MOTOR_36V);
        RB_MOTOR_36V.setText("36V");

        CB_ASS_WITHOUT_PED.setText("Startup assist without pedaling");
        CB_ASS_WITHOUT_PED.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_ASS_WITHOUT_PEDStateChanged(evt);
            }
        });

        jLabel20.setText("Startup boost torque factor (%)");

        TF_BOOST_TORQUE_FACTOR.setText("300");
        TF_BOOST_TORQUE_FACTOR.setToolTipText("<html>Max value 500<br>\nRecommended range 200 to 300\n</html>");
        TF_BOOST_TORQUE_FACTOR.setEnabled(CB_STARTUP_BOOST_ON_START.isSelected());

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 0, 0));
        jLabel3.setText("Motor acceleration (%)");

        TF_MOTOR_ACC.setText("25");
        TF_MOTOR_ACC.setToolTipText("<html>MAX VALUE<br>\n36 volt motor, 36 volt battery = 35<br>\n36 volt motor, 48 volt battery = 5<br>\n36 volt motor, 52 volt battery = 0<br>\n48 volt motor, 36 volt battery = 45<br>\n48 volt motor, 48 volt battery = 35<br>\n48 volt motor, 52 volt battery = 30\n</html>");

        TF_ASS_WITHOUT_PED_THRES.setText("20");
        TF_ASS_WITHOUT_PED_THRES.setToolTipText("<html>Max value 100<br>\nRecommended range 10 to 30\n</html>");
        TF_ASS_WITHOUT_PED_THRES.setEnabled(CB_ASS_WITHOUT_PED.isSelected());

        jLabel22.setText("Startup boost cadence step (decr.)");

        TF_BOOST_CADENCE_STEP.setText("20");
        TF_BOOST_CADENCE_STEP.setToolTipText("<html>Max value 50<br>\nRecommended range 20 to 30<br>\n(high values short effect)\n</html>");
        TF_BOOST_CADENCE_STEP.setEnabled(CB_STARTUP_BOOST_ON_START.isSelected());

        buttonGroup1.add(RB_MOTOR_48V);
        RB_MOTOR_48V.setText("48V");

        jLabelMotorFastStop.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelMotorFastStop.setForeground(new java.awt.Color(255, 0, 0));
        jLabelMotorFastStop.setText("Motor deceleration (%)");

        TF_MOTOR_DEC.setText("0");
        TF_MOTOR_DEC.setToolTipText("<html>Max value 100<br>\nRecommended range 0 to 50\n</html>");

        jLabel33.setText("Startup boost at zero");

        buttonGroup9.add(RB_BOOST_AT_ZERO_CADENCE);
        RB_BOOST_AT_ZERO_CADENCE.setText("cadence");
        RB_BOOST_AT_ZERO_CADENCE.setEnabled(CB_STARTUP_BOOST_ON_START.isSelected());

        buttonGroup9.add(RB_BOOST_AT_ZERO_SPEED);
        RB_BOOST_AT_ZERO_SPEED.setText("speed");
        RB_BOOST_AT_ZERO_SPEED.setEnabled(CB_STARTUP_BOOST_ON_START.isSelected());

        CB_STARTUP_BOOST_ON_START.setText("Startup boost enabled  on startup");
        CB_STARTUP_BOOST_ON_START.setToolTipText("Used only in Power assist mode");
        CB_STARTUP_BOOST_ON_START.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_STARTUP_BOOST_ON_STARTStateChanged(evt);
            }
        });

        CB_FIELD_WEAKENING_ENABLED.setText("Field weakening enabled");
        CB_FIELD_WEAKENING_ENABLED.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_FIELD_WEAKENING_ENABLEDStateChanged(evt);
            }
        });

        CB_STARTUP_ASSIST_ENABLED.setText("Startup assist enabled");
        CB_STARTUP_ASSIST_ENABLED.setToolTipText("Not available with coaster brake enabled");
        CB_STARTUP_ASSIST_ENABLED.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_STARTUP_ASSIST_ENABLEDStateChanged(evt);
            }
        });

        jLabel88.setText("Startup assist w/o pedaling threshold");
        jLabel88.setInheritsPopupMenu(false);

        CB_SMOOTH_START_ENABLED.setText("Smooth start enabled");
        CB_SMOOTH_START_ENABLED.setToolTipText("<html>Used in Torque and Hybrid assist mode (disableable)<br>\nUsed inCadence assist mode (cannot be disabled)\n</html>\n");
        CB_SMOOTH_START_ENABLED.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_SMOOTH_START_ENABLEDStateChanged(evt);
            }
        });

        jLabel77.setText("Smooth start ramp (%)");

        TF_SMOOTH_START_RAMP.setText("35");
        TF_SMOOTH_START_RAMP.setToolTipText("<html>Max value 100<br>\nRecommended range 0 to 50\n</html>");
        TF_SMOOTH_START_RAMP.setEnabled(CB_SMOOTH_START_ENABLED.isSelected());

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(Label_Parameter1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(54, 54, 54)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(RB_MOTOR_36V, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(RB_MOTOR_48V, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(CB_ASS_WITHOUT_PED, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel88, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TF_ASS_WITHOUT_PED_THRES, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(TF_MOTOR_ACC, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabelMotorFastStop, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_MOTOR_DEC, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(RB_BOOST_AT_ZERO_CADENCE)
                        .addGap(6, 6, 6)
                        .addComponent(RB_BOOST_AT_ZERO_SPEED)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CB_STARTUP_BOOST_ON_START)
                            .addComponent(CB_SMOOTH_START_ENABLED)
                            .addComponent(jLabel7)
                            .addComponent(CB_FIELD_WEAKENING_ENABLED)
                            .addComponent(CB_STARTUP_ASSIST_ENABLED, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel77, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(TF_SMOOTH_START_RAMP, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_BOOST_CADENCE_STEP, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_BOOST_TORQUE_FACTOR, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(RB_MOTOR_36V)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(RB_MOTOR_48V)
                        .addGap(4, 4, 4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(Label_Parameter1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_MOTOR_ACC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMotorFastStop, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_MOTOR_DEC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_FIELD_WEAKENING_ENABLED)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CB_ASS_WITHOUT_PED)
                .addGap(2, 2, 2)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_ASS_WITHOUT_PED_THRES, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel88))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_STARTUP_BOOST_ON_START)
                .addGap(6, 6, 6)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_BOOST_TORQUE_FACTOR, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_BOOST_CADENCE_STEP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RB_BOOST_AT_ZERO_CADENCE)
                    .addComponent(RB_BOOST_AT_ZERO_SPEED))
                .addGap(8, 8, 8)
                .addComponent(CB_SMOOTH_START_ENABLED)
                .addGap(3, 3, 3)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel77, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_SMOOTH_START_RAMP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(CB_STARTUP_ASSIST_ENABLED)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel18.setText("Battery settings");

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 0, 0));
        jLabel21.setText("Battery capacity (Wh)");

        TF_BATT_CAPACITY.setText("630");
        TF_BATT_CAPACITY.setToolTipText("<html>To calculate<br>\nBattery Volt x Ah\n</html>\n");

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 0, 0));
        jLabel9.setText("Battery current max (A)");

        TF_BAT_CUR_MAX.setText("17");
        TF_BAT_CUR_MAX.setToolTipText("<html>Max value<br>\n17 A for 36 V<br>\n12 A for 48 V\n</html>");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 0, 0));
        jLabel10.setText("Battery power max (W)");

        TF_BATT_POW_MAX.setText("500");
        TF_BATT_POW_MAX.setToolTipText("<html>Motor power limit in offroad mode<br>\nMax value depends on the rated<br>\nmotor power and the battery capacity\n</html>");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 0, 0));
        jLabel11.setText("Battery cells number (series)");

        TF_BATT_NUM_CELLS.setText("10");
        TF_BATT_NUM_CELLS.setToolTipText("<html> 7 for 24 V battery<br>\n10 for 36 V battery<br>\n13 for 48 V battery<br>\n14 for 52 V battery\n</html>");

        jLabel24.setText("Battery voltage calibration (%)");

        TF_BATT_VOLT_CAL.setText("100");
        TF_BATT_VOLT_CAL.setToolTipText("<html>For calibrate voltage displayed<br>\nIndicative value 95 to 105\n</html>");

        jLabel25.setText("Battery capacity calibration (%)");

        TF_BATT_CAPACITY_CAL.setText("100");
        TF_BATT_CAPACITY_CAL.setToolTipText("<html>Starting to 100%<br>\nwith the% remaining when battery is low<br>\ncalculate the actual%\n</html>");

        TF_BATT_VOLT_CUT_OFF.setText("29");
        TF_BATT_VOLT_CUT_OFF.setToolTipText("<html>Indicative value 29 for 36 V<br>\n38 for 48 V, It depends on the<br>\ncharacteristics of the battery\n</html>");

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 0, 0));
        jLabel12.setText("Battery voltage cut off (V)");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(TF_BAT_CUR_MAX, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(TF_BATT_NUM_CELLS, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(TF_BATT_POW_MAX, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(TF_BATT_CAPACITY, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(TF_BATT_VOLT_CAL, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TF_BATT_VOLT_CUT_OFF, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(TF_BATT_CAPACITY_CAL, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CUR_MAX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BATT_POW_MAX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BATT_CAPACITY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BATT_NUM_CELLS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BATT_VOLT_CUT_OFF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BATT_VOLT_CAL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BATT_CAPACITY_CAL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        Label_Parameter3.setText("Mode");

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel16.setText("Display settings");

        Label_Parameter2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        Label_Parameter2.setForeground(new java.awt.Color(255, 0, 0));
        Label_Parameter2.setText("Display type");

        buttonGroup4.add(RB_DISPLAY_WORK_ON);
        RB_DISPLAY_WORK_ON.setText("Working on");

        buttonGroup4.add(RB_DISPLAY_ALWAY_ON);
        RB_DISPLAY_ALWAY_ON.setText("Always on");

        Label_Parameter5.setText("Units type");
        Label_Parameter5.setFocusable(false);

        JCB_DISPLAY_TYPE.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "VLCD5", "VLCD6", "XH18", "850C" }));
        JCB_DISPLAY_TYPE.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_DISPLAY_TYPEItemStateChanged(evt);
            }
        });

        CB_ODO_COMPENSATION.setText("Odometer compensation");

        CB_SET_PARAM_ON_START.setText("Set parameters on startup");

        CB_AUTO_DISPLAY_DATA.setText("Auto display data with lights on");
        CB_AUTO_DISPLAY_DATA.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_AUTO_DISPLAY_DATAStateChanged(evt);
            }
        });

        JCB_UNITS_TYPE.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "km/h", "mph", "alt. mph" }));
        JCB_UNITS_TYPE.setToolTipText("For alternative mph, set km/h on display");
        JCB_UNITS_TYPE.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_UNITS_TYPEItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(CB_AUTO_DISPLAY_DATA)
                            .addComponent(CB_ODO_COMPENSATION)
                            .addComponent(CB_SET_PARAM_ON_START)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(Label_Parameter3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(RB_DISPLAY_WORK_ON)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RB_DISPLAY_ALWAY_ON, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 12, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(Label_Parameter2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(JCB_DISPLAY_TYPE, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(Label_Parameter5, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(JCB_UNITS_TYPE, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel16)
                .addGap(1, 1, 1)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Label_Parameter2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(JCB_DISPLAY_TYPE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Label_Parameter3)
                    .addComponent(RB_DISPLAY_WORK_ON)
                    .addComponent(RB_DISPLAY_ALWAY_ON))
                .addGap(5, 5, 5)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Label_Parameter5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(JCB_UNITS_TYPE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_ODO_COMPENSATION)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CB_SET_PARAM_ON_START)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CB_AUTO_DISPLAY_DATA)
                .addGap(66, 66, 66))
        );

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel39.setText("Torque sensor settings");

        CB_TOR_SENSOR_ADV.setText("Torque sensor adv.");
        CB_TOR_SENSOR_ADV.setEnabled(CB_TORQUE_CALIBRATION.isSelected());

        CB_TORQUE_CALIBRATION.setText("Calibrated");
        CB_TORQUE_CALIBRATION.setToolTipText("Enable after calibration");
        CB_TORQUE_CALIBRATION.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_TORQUE_CALIBRATIONStateChanged(evt);
            }
        });

        TF_TORQ_ADC_OFFSET_ADJ.setText("0");
        TF_TORQ_ADC_OFFSET_ADJ.setToolTipText("<html>\nValue -20 to 14<br>\nDefault 0\n</html>");
        TF_TORQ_ADC_OFFSET_ADJ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_TORQ_ADC_OFFSET_ADJKeyReleased(evt);
            }
        });

        jLabel31.setText("Pedal torque ADC step advanced");

        jLabel6.setText("Pedal torque ADC step");

        TF_TORQ_ADC_RANGE_ADJ.setText("0");
        TF_TORQ_ADC_RANGE_ADJ.setToolTipText("<html>\nValue -20 to 20<br>\nDefault 0\n</html>");
        TF_TORQ_ADC_RANGE_ADJ.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        TF_TORQ_ADC_RANGE_ADJ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_TORQ_ADC_RANGE_ADJKeyReleased(evt);
            }
        });

        jLabel23.setText("Pedal torque ADC range adjustment");

        TF_TORQ_PER_ADC_STEP.setText("67");
        TF_TORQ_PER_ADC_STEP.setToolTipText("<html>\nDefault value 67<br>\nOptional calibration\n</html>");
        TF_TORQ_PER_ADC_STEP.setEnabled(!CB_ADC_STEP_ESTIM.isSelected());

        TF_TORQUE_ADC_MAX.setText("300");
        TF_TORQUE_ADC_MAX.setToolTipText("<html>\nInsert value read on calibration<br>\nMax 500\n</html>");
        TF_TORQUE_ADC_MAX.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        TF_TORQUE_ADC_MAX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_TORQUE_ADC_MAXKeyReleased(evt);
            }
        });

        jLabel_TORQ_ADC_MAX.setText("Pedal torque ADC max (max weight)");

        jLabel15.setText("Pedal torque ADC offset adjustment");

        jLabel_TORQ_ADC_OFFSET.setText("Pedal torque ADC offset (no weight)");

        CB_ADC_STEP_ESTIM.setText("Estimated");
        CB_ADC_STEP_ESTIM.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        CB_ADC_STEP_ESTIM.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_ADC_STEP_ESTIMStateChanged(evt);
            }
        });

        TF_TORQ_ADC_OFFSET.setText("150");
        TF_TORQ_ADC_OFFSET.setToolTipText("<html>\nInsert value read on calibration<br>\nMax 250\n</html>");
        TF_TORQ_ADC_OFFSET.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        TF_TORQ_ADC_OFFSET.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_TORQ_ADC_OFFSETKeyReleased(evt);
            }
        });

        TF_TORQ_PER_ADC_STEP_ADV.setText("34");
        TF_TORQ_PER_ADC_STEP_ADV.setToolTipText("<html>\nDefault value 34<br>\nOptional calibration\n</html>");
        TF_TORQ_PER_ADC_STEP_ADV.setEnabled(CB_TORQUE_CALIBRATION.isSelected());

        TF_TORQ_ADC_ANGLE_ADJ.setText("0");
        TF_TORQ_ADC_ANGLE_ADJ.setToolTipText("<html>\nValue -20 to 20<br>\nDefault 0\n</html>");
        TF_TORQ_ADC_ANGLE_ADJ.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        TF_TORQ_ADC_ANGLE_ADJ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_TORQ_ADC_ANGLE_ADJKeyReleased(evt);
            }
        });

        jLabel32.setText("Pedal torque ADC angle adjustment");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel13.setText("Bike settings");

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 0, 0));
        jLabel14.setText("Wheel circumference (mm)");

        TF_WHEEL_CIRCUMF.setText("2260");
        TF_WHEEL_CIRCUMF.setToolTipText("<html>Indicative values:<br>\n26-inch wheel = 2050 mm<br>\n27-inch wheel = 2150 mm<br>\n27.5 inch wheel = 2215 mm<br>\n28-inch wheel = 2250 mm<br>\n29-inch wheel = 2300 mmV\n</html>");

        jLabelMaxSpeed.setText("Max speed offroad mode");

        TF_MAX_SPEED.setText("25");
        TF_MAX_SPEED.setToolTipText("<html>km/h or mph<br>\nMax value in EU 25 km/h\n</html>");
        TF_MAX_SPEED.setEnabled(!CB_MAX_SPEED_DISPLAY.isSelected());
        TF_MAX_SPEED.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_MAX_SPEEDKeyReleased(evt);
            }
        });

        CB_MAX_SPEED_DISPLAY.setText("Set max speed from display");
        CB_MAX_SPEED_DISPLAY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_MAX_SPEED_DISPLAYStateChanged(evt);
            }
        });

        jLabel79.setText("Assist mode on startup");

        JCB_ASSIST_MODE_ON_STARTUP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Power", "Torque", "Cadence", "eMTB", "Hybrid" }));
        JCB_ASSIST_MODE_ON_STARTUP.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_ASSIST_MODE_ON_STARTUPItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(CB_TOR_SENSOR_ADV)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(CB_TORQUE_CALIBRATION))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(CB_ADC_STEP_ESTIM))))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                            .addComponent(jLabel_TORQ_ADC_MAX, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel32, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel_TORQ_ADC_OFFSET, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TF_TORQ_ADC_OFFSET, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(TF_TORQ_ADC_RANGE_ADJ, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(TF_TORQ_ADC_OFFSET_ADJ, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(TF_TORQ_PER_ADC_STEP_ADV, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(TF_TORQ_ADC_ANGLE_ADJ, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGap(1, 1, 1)
                                    .addComponent(TF_TORQ_PER_ADC_STEP, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(TF_TORQUE_ADC_MAX, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_MAX_SPEED_DISPLAY)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel79, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelMaxSpeed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(JCB_ASSIST_MODE_ON_STARTUP, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TF_WHEEL_CIRCUMF, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(TF_MAX_SPEED, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CB_TOR_SENSOR_ADV)
                    .addComponent(CB_TORQUE_CALIBRATION))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TF_TORQ_PER_ADC_STEP)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CB_ADC_STEP_ESTIM, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_TORQ_PER_ADC_STEP_ADV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_TORQ_ADC_OFFSET_ADJ, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_TORQ_ADC_RANGE_ADJ, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_TORQ_ADC_ANGLE_ADJ, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_TORQ_ADC_OFFSET, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_TORQ_ADC_OFFSET))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_TORQUE_ADC_MAX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_TORQ_ADC_MAX))
                .addGap(12, 12, 12)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_WHEEL_CIRCUMF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_MAX_SPEED, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMaxSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(CB_MAX_SPEED_DISPLAY)
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel79)
                    .addComponent(JCB_ASSIST_MODE_ON_STARTUP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(45, 45, 45))
        );

        jTabbedPane1.addTab("Basic settings", jPanel1);

        jPanel4.setPreferredSize(new java.awt.Dimension(844, 552));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText("Power assist mode");

        jLabel26.setText("Assist level 1 - ECO");

        TF_POWER_ASS_1.setText("70");
        TF_POWER_ASS_1.setToolTipText("<html>% Human power<br>\nMax value 500\n</html>");

        jLabel27.setText("Assist level 2 - TOUR");

        TF_POWER_ASS_2.setText("120");
        TF_POWER_ASS_2.setToolTipText("<html>% Human power<br>\nMax value 500\n</html>");

        jLabel28.setText("Assist level 3 - SPORT");

        TF_POWER_ASS_3.setText("210");
        TF_POWER_ASS_3.setToolTipText("<html>% Human power<br>\nMax value 500\n</html>");

        TF_POWER_ASS_4.setText("300");
        TF_POWER_ASS_4.setToolTipText("<html>% Human power<br>\nMax value 500\n</html>");

        jLabel29.setText("Assist level 4 -TURBO");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_POWER_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_POWER_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_POWER_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_POWER_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(TF_POWER_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(TF_POWER_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(TF_POWER_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(TF_POWER_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel43.setText("Torque assist mode");

        jLabel44.setText("Assist level 1 - ECO");

        TF_TORQUE_ASS_1.setText("70");
        TF_TORQUE_ASS_1.setToolTipText("Max value 254");

        jLabel45.setText("Assist level 2 - TOUR");

        TF_TORQUE_ASS_2.setText("100");
        TF_TORQUE_ASS_2.setToolTipText("Max value 254");

        jLabel46.setText("Assist level 3 - SPORT");

        TF_TORQUE_ASS_3.setText("130");
        TF_TORQUE_ASS_3.setToolTipText("Max value 254");

        TF_TORQUE_ASS_4.setText("160");
        TF_TORQUE_ASS_4.setToolTipText("Max value 254");

        jLabel47.setText("Assist level 4 -TURBO");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel43, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel44)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_TORQUE_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel45)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_TORQUE_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel46)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_TORQUE_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel47)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TF_TORQUE_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(TF_TORQUE_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(TF_TORQUE_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(TF_TORQUE_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47)
                    .addComponent(TF_TORQUE_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel48.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel48.setText("Cadence assist mode");

        jLabel49.setText("Assist level 1 - ECO");

        TF_CADENCE_ASS_1.setText("70");
        TF_CADENCE_ASS_1.setToolTipText("Max value 254");

        jLabel50.setText("Assist level 2 - TOUR");

        TF_CADENCE_ASS_2.setText("100");
        TF_CADENCE_ASS_2.setToolTipText("Max value 254");

        jLabel51.setText("Assist level 3 - SPORT");

        TF_CADENCE_ASS_3.setText("130");
        TF_CADENCE_ASS_3.setToolTipText("Max value 254");

        TF_CADENCE_ASS_4.setText("160");
        TF_CADENCE_ASS_4.setToolTipText("Max value 254");

        jLabel52.setText("Assist level 4 -TURBO");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel48, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(6, 6, 6))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel52)
                            .addComponent(jLabel51)
                            .addComponent(jLabel49)
                            .addComponent(jLabel50))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TF_CADENCE_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_CADENCE_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_CADENCE_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_CADENCE_ASS_4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel49)
                    .addComponent(TF_CADENCE_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel50)
                    .addComponent(TF_CADENCE_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel51)
                    .addComponent(TF_CADENCE_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel52)
                    .addComponent(TF_CADENCE_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel53.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel53.setText("eMTB assist mode");

        jLabel54.setText("Assist level 1 - ECO");

        TF_EMTB_ASS_1.setText("60");
        TF_EMTB_ASS_1.setToolTipText("<html>Value<br>\nbetween 21 to 254\n</html>");
        TF_EMTB_ASS_1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                TF_EMTB_ASS_1FocusLost(evt);
            }
        });

        jLabel55.setText("Assist level 2 - TOUR");

        TF_EMTB_ASS_2.setText("120");
        TF_EMTB_ASS_2.setToolTipText("<html>Value<br>\nbetween 21 to 254\n</html>");
        TF_EMTB_ASS_2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                TF_EMTB_ASS_2FocusLost(evt);
            }
        });

        jLabel56.setText("Assist level 3 - SPORT");

        TF_EMTB_ASS_3.setText("180");
        TF_EMTB_ASS_3.setToolTipText("<html>Value<br>\nbetween 21 to 254\n</html>");
        TF_EMTB_ASS_3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                TF_EMTB_ASS_3FocusLost(evt);
            }
        });

        TF_EMTB_ASS_4.setText("240");
        TF_EMTB_ASS_4.setToolTipText("<html>Value<br>\nbetween 21 to 254\n</html>");
        TF_EMTB_ASS_4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                TF_EMTB_ASS_4FocusLost(evt);
            }
        });

        jLabel57.setText("Assist level 4 -TURBO");

        buttonGroup2.add(RB_eMTB_POWER);
        RB_eMTB_POWER.setText("Power");

        jLabel78.setText("eMTB based");

        buttonGroup2.add(RB_eMTB_TORQUE);
        RB_eMTB_TORQUE.setText("Torque");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel53, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(jLabel54)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_EMTB_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(jLabel55)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_EMTB_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(jLabel56)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_EMTB_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                                .addComponent(jLabel57)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_EMTB_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(12, 12, 12))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel78, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(RB_eMTB_POWER)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(RB_eMTB_TORQUE)
                        .addContainerGap(10, Short.MAX_VALUE))))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54)
                    .addComponent(TF_EMTB_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel55)
                    .addComponent(TF_EMTB_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel56)
                    .addComponent(TF_EMTB_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel57)
                    .addComponent(TF_EMTB_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel78)
                    .addComponent(RB_eMTB_POWER)
                    .addComponent(RB_eMTB_TORQUE)))
        );

        jLabel62.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel62.setText("Walk assist mode");

        jLabel63.setText("Speed level 1 - ECO");

        TF_WALK_ASS_SPEED_1.setText("35");
        TF_WALK_ASS_SPEED_1.setToolTipText("<html>km/h x10 or mph x10<br>\nValue 35 to 50 (3.5 to 5.0 km/h)\n</html>");
        TF_WALK_ASS_SPEED_1.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_WALK_ASS_SPEED_1KeyReleased(evt);
            }
        });

        jLabel64.setText("Speed level 2 - TOUR");

        TF_WALK_ASS_SPEED_2.setText("40");
        TF_WALK_ASS_SPEED_2.setToolTipText("<html>km/h x10 or mph x10<br>\nValue 35 to 50 (3.5 to 5.0 km/h)\n</html>");
        TF_WALK_ASS_SPEED_2.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_WALK_ASS_SPEED_2KeyReleased(evt);
            }
        });

        jLabel65.setText("Speed level 3 - SPORT");

        TF_WALK_ASS_SPEED_3.setText("45");
        TF_WALK_ASS_SPEED_3.setToolTipText("<html>km/h x10 or mph x10<br>\nValue 35 to 50 (3.5 to 5.0 km/h)\n</html>");
        TF_WALK_ASS_SPEED_3.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_WALK_ASS_SPEED_3KeyReleased(evt);
            }
        });

        TF_WALK_ASS_SPEED_4.setText("50");
        TF_WALK_ASS_SPEED_4.setToolTipText("<html>km/h x10 or mph x10<br>\nValue 35 to 50 (3.5 to 5.0 km/h)\n</html>");
        TF_WALK_ASS_SPEED_4.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_WALK_ASS_SPEED_4KeyReleased(evt);
            }
        });

        jLabel66.setText("Speed level 4 -TURBO");

        jLabelWalkSpeed.setText("Walk assist speed limit");

        TF_WALK_ASS_SPEED_LIMIT.setText("60");
        TF_WALK_ASS_SPEED_LIMIT.setToolTipText("<html>km/h x10 or mph x10<br>\nMax value 60 (in EU 6 km/h)\n</html>");
        TF_WALK_ASS_SPEED_LIMIT.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_LIMIT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_WALK_ASS_SPEED_LIMITKeyReleased(evt);
            }
        });

        TF_WALK_ASS_TIME.setText("60");
        TF_WALK_ASS_TIME.setToolTipText("Max value 255 (0.1 s)\n\n");
        TF_WALK_ASS_TIME.setEnabled(((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR) || (JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)) && CB_WALK_ASSIST_ENABLED.isSelected() && CB_WALK_TIME_ENA.isSelected());

        jLabel68.setText("Walk assist deb. time");

        CB_WALK_TIME_ENA.setText("Walk assist debounce time");
        CB_WALK_TIME_ENA.setToolTipText("Only with brake sensors enabled and offroad mode");
        CB_WALK_TIME_ENA.setEnabled(((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR) || (JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)) && CB_WALK_ASSIST_ENABLED.isSelected());
        CB_WALK_TIME_ENA.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_WALK_TIME_ENAStateChanged(evt);
            }
        });

        jLabelWalkSpeedUnits.setText("km/h x10");

        CB_WALK_ASSIST_ENABLED.setText("Walk assist enabled");
        CB_WALK_ASSIST_ENABLED.setToolTipText("Not available with coaster brake enabled");
        CB_WALK_ASSIST_ENABLED.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_WALK_ASSIST_ENABLEDStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addComponent(jLabel62)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabelWalkSpeedUnits))
                            .addComponent(CB_WALK_TIME_ENA)
                            .addComponent(CB_WALK_ASSIST_ENABLED, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(11, Short.MAX_VALUE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel68)
                            .addComponent(jLabelWalkSpeed)
                            .addComponent(jLabel66)
                            .addComponent(jLabel65)
                            .addComponent(jLabel64)
                            .addComponent(jLabel63))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TF_WALK_ASS_SPEED_1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_WALK_ASS_SPEED_2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_WALK_ASS_SPEED_3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_WALK_ASS_SPEED_4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_WALK_ASS_SPEED_LIMIT, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_WALK_ASS_TIME, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel62, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelWalkSpeedUnits, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_WALK_ASSIST_ENABLED)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel63)
                    .addComponent(TF_WALK_ASS_SPEED_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel64)
                    .addComponent(TF_WALK_ASS_SPEED_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
                    .addComponent(TF_WALK_ASS_SPEED_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel66)
                    .addComponent(TF_WALK_ASS_SPEED_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelWalkSpeed)
                    .addComponent(TF_WALK_ASS_SPEED_LIMIT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel68)
                    .addComponent(TF_WALK_ASS_TIME, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_WALK_TIME_ENA)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel69.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel69.setText("Cruise mode");

        jLabel70.setText("Speed level 1 - ECO");

        TF_CRUISE_ASS_1.setText("15");
        TF_CRUISE_ASS_1.setToolTipText("<html>km/h or mph<br>\nMax value in EU 25 km/h\n</html>");
        TF_CRUISE_ASS_1.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_ASS_1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_CRUISE_ASS_1KeyReleased(evt);
            }
        });

        jLabel71.setText("Speed level 2 - TOUR");

        TF_CRUISE_ASS_2.setText("18");
        TF_CRUISE_ASS_2.setToolTipText("<html>km/h or mph<br>\nMax value in EU 25 km/h\n</html>");
        TF_CRUISE_ASS_2.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_ASS_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_CRUISE_ASS_2KeyReleased(evt);
            }
        });

        jLabel72.setText("Speed level 3 - SPORT");

        TF_CRUISE_ASS_3.setText("21");
        TF_CRUISE_ASS_3.setToolTipText("<html>km/h or mph<br>\nMax value in EU 25 km/h\n</html>");
        TF_CRUISE_ASS_3.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_ASS_3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_CRUISE_ASS_3KeyReleased(evt);
            }
        });

        TF_CRUISE_ASS_4.setText("24");
        TF_CRUISE_ASS_4.setToolTipText("<html>km/h or mph<br>\nMax value in EU 25 km/h\n</html>");
        TF_CRUISE_ASS_4.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_ASS_4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_CRUISE_ASS_4KeyReleased(evt);
            }
        });

        jLabel73.setText("Speed level 4 -TURBO");

        jLabel74.setText("Speed cruise enabled");

        TF_CRUISE_SPEED_ENA.setText("10");
        TF_CRUISE_SPEED_ENA.setToolTipText("Min speed to enable cruise (km/h or mph)");
        TF_CRUISE_SPEED_ENA.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_SPEED_ENA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_CRUISE_SPEED_ENAKeyReleased(evt);
            }
        });

        CB_CRUISE_WHITOUT_PEDALING.setText("Cruise without pedaling");
        CB_CRUISE_WHITOUT_PEDALING.setToolTipText("Only with brake sensors enabled");
        CB_CRUISE_WHITOUT_PEDALING.setEnabled(((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR) || (JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)) && CB_CRUISE_ENABLED.isSelected());
        CB_CRUISE_WHITOUT_PEDALING.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_CRUISE_WHITOUT_PEDALINGStateChanged(evt);
            }
        });

        jLabelCruiseSpeedUnits.setText("km/h");

        CB_CRUISE_ENABLED.setText("Cruise enabled");
        CB_CRUISE_ENABLED.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_CRUISE_ENABLEDStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(CB_CRUISE_ENABLED, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel69, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                                .addComponent(jLabelCruiseSpeedUnits)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(CB_CRUISE_WHITOUT_PEDALING)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel70)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_CRUISE_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel71)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_CRUISE_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel72)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 20, Short.MAX_VALUE)
                                .addComponent(TF_CRUISE_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel73)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_CRUISE_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel74)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TF_CRUISE_SPEED_ENA, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel69, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCruiseSpeedUnits))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_CRUISE_ENABLED)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel70)
                    .addComponent(TF_CRUISE_ASS_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel71)
                    .addComponent(TF_CRUISE_ASS_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel72)
                    .addComponent(TF_CRUISE_ASS_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel73)
                    .addComponent(TF_CRUISE_ASS_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel74)
                    .addComponent(TF_CRUISE_SPEED_ENA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_CRUISE_WHITOUT_PEDALING)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel76.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel76.setText("Lights configuration");

        jLabelLights0.setText("Lights mode on startup");
        jLabelLights0.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        TF_LIGHT_MODE_ON_START.setText("0");
        TF_LIGHT_MODE_ON_START.setToolTipText("<html>With lights button ON<br>\n0 - lights ON<br>\n1 - lights FLASHING<br>\n2 - lights ON and BRAKE-FLASHING when braking<br>\n3 - lights FLASHING and ON when braking<br>\n4 - lights FLASHING and BRAKE-FLASHING when braking<br>\n5 - lights ON and ON when braking, even with the light button OFF<br>\n6 - lights ON and BRAKE-FLASHING when braking, even with the light button OFF<br>\n7 - lights FLASHING and ON when braking, even with the light button OFF<br>\n8 - lights FLASHING and BRAKE-FLASHING when braking, even with the light button OFF\n</html>");
        TF_LIGHT_MODE_ON_START.setEnabled(CB_LIGHTS.isSelected());
        TF_LIGHT_MODE_ON_START.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_LIGHT_MODE_ON_STARTKeyReleased(evt);
            }
        });

        jLabelLights1.setText("Lights mode 1");
        jLabelLights1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        TF_LIGHT_MODE_1.setText("6");
        TF_LIGHT_MODE_1.setToolTipText("<html>With lights button ON<br>\n0 - lights ON<br>\n1 - lights FLASHING<br>\n2 - lights ON and BRAKE-FLASHING when braking<br>\n3 - lights FLASHING and ON when braking<br>\n4 - lights FLASHING and BRAKE-FLASHING when braking<br>\n5 - lights ON and ON when braking, even with the light button OFF<br>\n6 - lights ON and BRAKE-FLASHING when braking, even with the light button OFF<br>\n7 - lights FLASHING and ON when braking, even with the light button OFF<br>\n8 - lights FLASHING and BRAKE-FLASHING when braking, even with the light button OFF\n</html>");
        TF_LIGHT_MODE_1.setEnabled(CB_LIGHTS.isSelected());
        TF_LIGHT_MODE_1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_LIGHT_MODE_1KeyReleased(evt);
            }
        });

        jLabelLights2.setText("Lights mode 2");
        jLabelLights2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        TF_LIGHT_MODE_2.setText("7");
        TF_LIGHT_MODE_2.setToolTipText("<html>With lights button ON<br>\n0 - lights ON<br>\n1 - lights FLASHING<br>\n2 - lights ON and BRAKE-FLASHING when braking<br>\n3 - lights FLASHING and ON when braking<br>\n4 - lights FLASHING and BRAKE-FLASHING when braking<br>\n5 - lights ON and ON when braking, even with the light button OFF<br>\n6 - lights ON and BRAKE-FLASHING when braking, even with the light button OFF<br>\n7 - lights FLASHING and ON when braking, even with the light button OFF<br>\n8 - lights FLASHING and BRAKE-FLASHING when braking, even with the light button OFF<br>\nor alternative option settings<br>\n9 - assistance without pedal rotation\n</html>");
        TF_LIGHT_MODE_2.setEnabled(CB_LIGHTS.isSelected());
        TF_LIGHT_MODE_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_LIGHT_MODE_2KeyReleased(evt);
            }
        });

        TF_LIGHT_MODE_3.setText("1");
        TF_LIGHT_MODE_3.setToolTipText("<html>With lights button ON<br>\n0 - lights ON<br>\n1 - lights FLASHING<br>\n2 - lights ON and BRAKE-FLASHING when braking<br>\n3 - lights FLASHING and ON when braking<br>\n4 - lights FLASHING and BRAKE-FLASHING when braking<br>\n5 - lights ON and ON when braking, even with the light button OFF<br>\n6 - lights ON and BRAKE-FLASHING when braking, even with the light button OFF<br>\n7 - lights FLASHING and ON when braking, even with the light button OFF<br>\n8 - lights FLASHING and BRAKE-FLASHING when braking, even with the light button OFF<br>\nor alternative option settings<br>\n10 - assistance with sensors error\n</html>");
        TF_LIGHT_MODE_3.setEnabled(CB_LIGHTS.isSelected());
        TF_LIGHT_MODE_3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_LIGHT_MODE_3KeyReleased(evt);
            }
        });

        jLabelLights3.setText("Lights mode 3");
        jLabelLights3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        CB_LIGHTS.setText("Lights enabled");
        CB_LIGHTS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_LIGHTSStateChanged(evt);
            }
        });

        jLabel67.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel67.setText("Assist levels 1/5 (%ECO)");

        TF_ASS_LEVELS_1_OF_5.setText("60");
        TF_ASS_LEVELS_1_OF_5.setToolTipText("");

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel76, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel67, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelLights3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabelLights1, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabelLights2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(CB_LIGHTS, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelLights0, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(2, 2, 2)
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(TF_LIGHT_MODE_1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(TF_LIGHT_MODE_2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(TF_ASS_LEVELS_1_OF_5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_LIGHT_MODE_3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_LIGHT_MODE_ON_START, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12))))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(jLabel76, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_LIGHTS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_LIGHT_MODE_ON_START, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelLights0, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLights1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_LIGHT_MODE_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLights2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_LIGHT_MODE_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLights3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_LIGHT_MODE_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel67, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_ASS_LEVELS_1_OF_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel17.setText("Street mode");

        jLabelStreetSpeed.setText("Street speed limit");

        TF_STREET_SPEED_LIM.setText("25");
        TF_STREET_SPEED_LIM.setToolTipText("<html>km/h or mph<br>\nMax value in EU 25 km/h\n</html>");
        TF_STREET_SPEED_LIM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_STREET_SPEED_LIMKeyReleased(evt);
            }
        });

        jLabel34.setForeground(new java.awt.Color(255, 0, 0));
        jLabel34.setText("Street power limit (W)");

        TF_STREET_POWER_LIM.setText("500");
        TF_STREET_POWER_LIM.setToolTipText("<html>Max nominal value in EU 250 W<br>\nMax peak value approx. 500 W\n</html>");
        TF_STREET_POWER_LIM.setEnabled(CB_STREET_POWER_LIM.isSelected());

        CB_STREET_POWER_LIM.setText("Street power limit enabled");
        CB_STREET_POWER_LIM.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CB_STREET_POWER_LIMStateChanged(evt);
            }
        });

        CB_STREET_CRUISE.setText("Cruise on street mode");
        CB_STREET_CRUISE.setEnabled(CB_CRUISE_ENABLED.isSelected());

        CB_STREET_WALK.setText("Walk assist on street mode");
        CB_STREET_WALK.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());

        CB_STREET_MODE_ON_START.setText("Street mode enabled on startup");

        JCB_THROTTLE_MODE_STREET.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Pedaling", "6 km/h only", "6 km/h & pedaling", "Unconditional" }));
        JCB_THROTTLE_MODE_STREET.setToolTipText("<html>Only with brake sensors enabled<br>\nTrottle mode must be at the same or higher level\n<html>");
        JCB_THROTTLE_MODE_STREET.setEnabled(((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR)||(JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH))&&(JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE));
        JCB_THROTTLE_MODE_STREET.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_THROTTLE_MODE_STREETItemStateChanged(evt);
            }
        });

        jLabel80.setText("Throttle on street mode");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel80, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(JCB_THROTTLE_MODE_STREET, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabelStreetSpeed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TF_STREET_POWER_LIM, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_STREET_SPEED_LIM, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CB_STREET_CRUISE)
                            .addComponent(CB_STREET_WALK)
                            .addComponent(CB_STREET_POWER_LIM)
                            .addComponent(CB_STREET_MODE_ON_START))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_STREET_MODE_ON_START)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelStreetSpeed)
                    .addComponent(TF_STREET_SPEED_LIM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(TF_STREET_POWER_LIM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_STREET_POWER_LIM)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CB_STREET_CRUISE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CB_STREET_WALK)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel80)
                .addGap(2, 2, 2)
                .addComponent(JCB_THROTTLE_MODE_STREET, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(61, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Assistance settings", jPanel4);

        jPanel8.setPreferredSize(new java.awt.Dimension(800, 486));

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel35.setText("Battery cells settings");

        jLabel36.setText("Cell voltage full (V)");

        TF_BAT_CELL_FULL.setText("3.95");
        TF_BAT_CELL_FULL.setToolTipText("Value 3.90 to 4.00");

        jLabel37.setText("Overvoltage (V)");

        TF_BAT_CELL_OVER.setText("4.35");
        TF_BAT_CELL_OVER.setToolTipText("Value 4.25 to 4.35");

        jLabel38.setText("Reset SOC percentage (V)");

        TF_BAT_CELL_SOC.setText("4.05");
        TF_BAT_CELL_SOC.setToolTipText("Value 4.00 to 4.10");

        jLabel40.setText("Cell voltage 3/4 (V)");

        TF_BAT_CELL_3_4.setText("3.70");
        TF_BAT_CELL_3_4.setToolTipText("Value empty to full");
        TF_BAT_CELL_3_4.setEnabled((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD6)||(JCB_DISPLAY_TYPE.getSelectedIndex() == XH18));

        jLabel41.setText("Cell voltage 2/4 (V)");

        TF_BAT_CELL_2_4.setText("3.45");
        TF_BAT_CELL_2_4.setToolTipText("Value empty to full");
        TF_BAT_CELL_2_4.setEnabled((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD6)||(JCB_DISPLAY_TYPE.getSelectedIndex() == XH18));

        jLabel42.setText("Cell voltage 1/4 (V)");

        TF_BAT_CELL_1_4.setText("3.25");
        TF_BAT_CELL_1_4.setToolTipText("Value empty to full");
        TF_BAT_CELL_1_4.setEnabled((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD6)||(JCB_DISPLAY_TYPE.getSelectedIndex() == XH18));

        jLabel75.setText("Cell voltage 5/6 (V)");

        TF_BAT_CELL_5_6.setText("3.85");
        TF_BAT_CELL_5_6.setToolTipText("Value empty to full");
        TF_BAT_CELL_5_6.setEnabled((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD5)||(JCB_DISPLAY_TYPE.getSelectedIndex() == C850));

        jLabel81.setText("Cell voltage 4/6 (V)");

        TF_BAT_CELL_4_6.setText("3.70");
        TF_BAT_CELL_4_6.setToolTipText("Value empty to full");
        TF_BAT_CELL_4_6.setEnabled((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD5)||(JCB_DISPLAY_TYPE.getSelectedIndex() == C850));

        jLabel82.setText("Cell voltage 3/6 (V)");

        TF_BAT_CELL_3_6.setText("3.55");
        TF_BAT_CELL_3_6.setToolTipText("Value empty to full");
        TF_BAT_CELL_3_6.setEnabled((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD5)||(JCB_DISPLAY_TYPE.getSelectedIndex() == C850));

        TF_BAT_CELL_2_6.setText("3.40");
        TF_BAT_CELL_2_6.setToolTipText("Value empty to full");
        TF_BAT_CELL_2_6.setEnabled((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD5)||(JCB_DISPLAY_TYPE.getSelectedIndex() == C850));

        jLabel83.setText("Cell voltage 2/6 (V)");

        TF_BAT_CELL_1_6.setText("3.25");
        TF_BAT_CELL_1_6.setToolTipText("Value empty to full");
        TF_BAT_CELL_1_6.setEnabled((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD5)||(JCB_DISPLAY_TYPE.getSelectedIndex() == C850));

        jLabel84.setText("Cell voltage 1/6 (V)");

        TF_BAT_CELL_EMPTY.setText("2.90");
        TF_BAT_CELL_EMPTY.setToolTipText("<html>Indicative value 2.90<br>\nIt depends on the<br>\ncharacteristics of the cells\n</html>");

        jLabel85.setText("Cell voltage empty (V)");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel35)
                        .addGap(64, 64, 64))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel42, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel75, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel81, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel82, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel83, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel84, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel85, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TF_BAT_CELL_OVER, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(TF_BAT_CELL_3_4, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(TF_BAT_CELL_2_4, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(TF_BAT_CELL_1_4, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(TF_BAT_CELL_5_6, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(TF_BAT_CELL_SOC, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(TF_BAT_CELL_FULL, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(TF_BAT_CELL_4_6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_BAT_CELL_3_6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_BAT_CELL_2_6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_BAT_CELL_1_6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_BAT_CELL_EMPTY, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel35)
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_OVER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_SOC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_FULL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_3_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel40))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_2_4)
                    .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_1_4)
                    .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_5_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel75))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_4_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel81, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_3_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel82, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_2_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel83, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_1_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel84, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_BAT_CELL_EMPTY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel85, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        jLabel86.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel86.setText("Display advanced settings");

        jLabel89.setText("Time to displayed data 1 (0.1 s)");

        TF_DELAY_DATA_1.setText("50");
        TF_DELAY_DATA_1.setToolTipText("<html>Max value 255 (0.1 sec)<br>\ncontinuous display at zero value\n</html>");

        jLabelData1.setText("Data 1");

        TF_DATA_1.setText("1");
        TF_DATA_1.setToolTipText("<html>0 - motor temperature (°C)<br>\n  1 - battery SOC remaining (%)<br>\n  2 - battery voltage (V)<br>\n  3 - battery current (A)<br>\n  4 - motor power (Watt/10)<br>\n  5 - adc throttle (8 bit)<br>\n  6 - adc torque sensor (10 bit)<br>\n  7 - pedal cadence (rpm)<br>\n  8 - human power(W/10)<br>\n  9 - pedal torque adc delta<br>\n10 - consumed Wh/10<br>\n11 - motor ERPS<br>\n12 - duty cycle PWM %\n</html>");
        TF_DATA_1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_DATA_1KeyReleased(evt);
            }
        });

        TF_DATA_2.setText("2");
        TF_DATA_2.setToolTipText("<html>0 - motor temperature (°C)<br>\n  1 - battery SOC remaining (%)<br>\n  2 - battery voltage (V)<br>\n  3 - battery current (A)<br>\n  4 - motor power (Watt/10)<br>\n  5 - adc throttle (8 bit)<br>\n  6 - adc torque sensor (10 bit)<br>\n  7 - pedal cadence (rpm)<br>\n  8 - human power(W/10)<br>\n  9 - pedal torque adc delta<br>\n10 - consumed Wh/10<br>\n11 - motor ERPS<br>\n12 - duty cycle PWM %\n</html>");
        TF_DATA_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_DATA_2KeyReleased(evt);
            }
        });

        jLabelData2.setText("Data 2");

        TF_DATA_3.setText("5");
        TF_DATA_3.setToolTipText("<html>0 - motor temperature (°C)<br>\n  1 - battery SOC remaining (%)<br>\n  2 - battery voltage (V)<br>\n  3 - battery current (A)<br>\n  4 - motor power (Watt/10)<br>\n  5 - adc throttle (8 bit)<br>\n  6 - adc torque sensor (10 bit)<br>\n  7 - pedal cadence (rpm)<br>\n  8 - human power(W/10)<br>\n  9 - pedal torque adc delta<br>\n10 - consumed Wh/10<br>\n11 - motor ERPS<br>\n12 - duty cycle PWM %\n</html>");
        TF_DATA_3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_DATA_3KeyReleased(evt);
            }
        });

        jLabelData3.setText("Data 3");

        jLabelData4.setText("Data 4");

        TF_DATA_4.setText("4");
        TF_DATA_4.setToolTipText("<html>0 - motor temperature (°C)<br>\n  1 - battery SOC remaining (%)<br>\n  2 - battery voltage (V)<br>\n  3 - battery current (A)<br>\n  4 - motor power (Watt/10)<br>\n  5 - adc throttle (8 bit)<br>\n  6 - adc torque sensor (10 bit)<br>\n  7 - pedal cadence (rpm)<br>\n  8 - human power(W/10)<br>\n  9 - pedal torque adc delta<br>\n10 - consumed Wh/10<br>\n11 - motor ERPS<br>\n12 - duty cycle PWM %\n</html>");
        TF_DATA_4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_DATA_4KeyReleased(evt);
            }
        });

        TF_DATA_5.setText("7");
        TF_DATA_5.setToolTipText("<html>0 - motor temperature (°C)<br>\n  1 - battery SOC remaining (%)<br>\n  2 - battery voltage (V)<br>\n  3 - battery current (A)<br>\n  4 - motor power (Watt/10)<br>\n  5 - adc throttle (8 bit)<br>\n  6 - adc torque sensor (10 bit)<br>\n  7 - pedal cadence (rpm)<br>\n  8 - human power(W/10)<br>\n  9 - pedal torque adc delta<br>\n10 - consumed Wh/10<br>\n11 - motor ERPS<br>\n12 - duty cycle PWM %\n</html>");
        TF_DATA_5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_DATA_5KeyReleased(evt);
            }
        });

        jLabelData5.setText("Data 5");

        jLabelData6.setText("Data 6");

        TF_DATA_6.setText("0");
        TF_DATA_6.setToolTipText("<html>0 - motor temperature (°C)<br>\n  1 - battery SOC remaining (%)<br>\n  2 - battery voltage (V)<br>\n  3 - battery current (A)<br>\n  4 - motor power (Watt/10)<br>\n  5 - adc throttle (8 bit)<br>\n  6 - adc torque sensor (10 bit)<br>\n  7 - pedal cadence (rpm)<br>\n  8 - human power(W/10)<br>\n  9 - pedal torque adc delta<br>\n10 - consumed Wh/10<br>\n11 - motor ERPS<br>\n12 - duty cycle PWM %\n</html>");
        TF_DATA_6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_DATA_6KeyReleased(evt);
            }
        });

        jLabel96.setText("Time to displayed data 2 (0.1 s)");

        TF_DELAY_DATA_2.setText("50");
        TF_DELAY_DATA_2.setToolTipText("<html>Max value 255 (0.1 sec)<br>\ncontinuous display at zero value\n</html>");

        jLabel97.setText("Time to displayed data 3 (0.1 s)");

        TF_DELAY_DATA_3.setText("50");
        TF_DELAY_DATA_3.setToolTipText("<html>Max value 255 (0.1 sec)<br>\ncontinuous display at zero value\n</html>");

        jLabel98.setText("Time to displayed data 4 (0.1 s)");

        TF_DELAY_DATA_4.setText("50");
        TF_DELAY_DATA_4.setToolTipText("<html>Max value 255 (0.1 sec)<br>\ncontinuous display at zero value\n</html>");

        jLabel99.setText("Time to displayed data 5 (0.1 s)");

        TF_DELAY_DATA_5.setText("50");
        TF_DELAY_DATA_5.setToolTipText("<html>Max value 255 (0.1 sec)<br>\ncontinuous display at zero value\n</html>");

        jLabel100.setText("Time to displayed data 6 (0.1 s)");

        TF_DELAY_DATA_6.setText("50");
        TF_DELAY_DATA_6.setToolTipText("<html>Max value 255 (0.1 sec)<br>\ncontinuous display at zero value\n</html>");

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabel89, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DELAY_DATA_1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabelData1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DATA_1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabel86)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabelData2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DATA_2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabelData3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DATA_3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabelData4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DATA_4, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabelData5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DATA_5, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabelData6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DATA_6, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabel96, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DELAY_DATA_2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabel97, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DELAY_DATA_3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabel98, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DELAY_DATA_4, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabel99, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DELAY_DATA_5, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabel100, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_DELAY_DATA_6, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel86)
                .addGap(18, 18, 18)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DATA_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelData1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DATA_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelData2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DATA_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelData3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DATA_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelData4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DATA_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelData5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DATA_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelData6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DELAY_DATA_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel89))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DELAY_DATA_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel96))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DELAY_DATA_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel97))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DELAY_DATA_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel98))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DELAY_DATA_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel99))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DELAY_DATA_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel100))
                .addContainerGap())
        );

        jLabel101.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel101.setText("Other function settings");

        jLabel102.setText("ADC throttle value      min");

        TF_ADC_THROTTLE_MIN.setText("47");
        TF_ADC_THROTTLE_MIN.setToolTipText("Value 40 to 50");
        TF_ADC_THROTTLE_MIN.setEnabled((JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE));

        TF_ADC_THROTTLE_MAX.setText("176");
        TF_ADC_THROTTLE_MAX.setToolTipText("Value 170 to 180");
        TF_ADC_THROTTLE_MAX.setEnabled((JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE));

        CB_TEMP_ERR_MIN_LIM.setText("Temperature error with min limit");
        CB_TEMP_ERR_MIN_LIM.setEnabled((JCB_OPTIONAL_ADC.getSelectedIndex() == TEMPERATURE));

        jLabel104.setText("Motor temperature min limit (°C)");

        TF_TEMP_MIN_LIM.setText("65");
        TF_TEMP_MIN_LIM.setToolTipText("Max value 75 (°C)");
        TF_TEMP_MIN_LIM.setEnabled((JCB_OPTIONAL_ADC.getSelectedIndex() == TEMPERATURE));

        jLabel105.setText("Motor temperature max limit (°C)");

        TF_TEMP_MAX_LIM.setText("85");
        TF_TEMP_MAX_LIM.setToolTipText("Max value 85 (°C)");
        TF_TEMP_MAX_LIM.setEnabled((JCB_OPTIONAL_ADC.getSelectedIndex() == TEMPERATURE));

        TF_DELAY_MENU.setText("50");
        TF_DELAY_MENU.setToolTipText("Max value 60 (0.1 s)");

        jLabel87.setText("Time to menu items (0.1 s)");

        jLabel90.setText("Number of data displayed at lights on");

        TF_NUM_DATA_AUTO_DISPLAY.setText("2");
        TF_NUM_DATA_AUTO_DISPLAY.setToolTipText("Value 1 to 6");
        TF_NUM_DATA_AUTO_DISPLAY.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_NUM_DATA_AUTO_DISPLAYKeyReleased(evt);
            }
        });

        jLabel109.setText("max");

        jLabel91.setText("Data displayed on startup");

        jLabelCoasterBrakeThreshld.setText("Coaster brake torque threshold");

        TF_COASTER_BRAKE_THRESHOLD.setText("15");
        TF_COASTER_BRAKE_THRESHOLD.setToolTipText("<htmlValue 5 to 40 (s)<br>\nRecommended 15<br>\n(high values short effect)\n</html>");
        TF_COASTER_BRAKE_THRESHOLD.setEnabled((JCB_BRAKE_FEATURE.getSelectedIndex() == COASTER_BRAKE));

        jLabel94.setText("Soc % calculation");

        JCB_DATA_ON_STARTUP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Soc %", "Volts" }));
        JCB_DATA_ON_STARTUP.setSelectedIndex(1);
        JCB_DATA_ON_STARTUP.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_DATA_ON_STARTUPItemStateChanged(evt);
            }
        });

        JCB_SOC_CALC.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Auto", "Wh", "Volts" }));
        JCB_SOC_CALC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_SOC_CALCItemStateChanged(evt);
            }
        });

        jLabel95.setText("Brake feature");

        JCB_BRAKE_FEATURE.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Brake sensors", "Coaster brake", "Temperature switch" }));
        JCB_BRAKE_FEATURE.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_BRAKE_FEATUREItemStateChanged(evt);
            }
        });

        jLabel30.setText("Optional ADC in");

        JCB_OPTIONAL_ADC.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Throttle", "Temperature sensor" }));
        JCB_OPTIONAL_ADC.setToolTipText("");
        JCB_OPTIONAL_ADC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_OPTIONAL_ADCItemStateChanged(evt);
            }
        });

        jLabel106.setText("Throttle mode");

        JCB_THROTTLE_MODE.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Pedaling", "6 km/h only", "6 km/h & pedaling", "Unconditional" }));
        JCB_THROTTLE_MODE.setToolTipText("<html>Only with brake sensors enabled<br>\nSet Optional ADC to Throttle\n<html>");
        JCB_THROTTLE_MODE.setEnabled(((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR)||(JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH))&&(JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE));
        JCB_THROTTLE_MODE.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_THROTTLE_MODEItemStateChanged(evt);
            }
        });

        jLabel107.setText("Temperature sensor type");

        JCB_TEMP_SENSOR_TYPE.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "LM35", "TMP36" }));
        JCB_TEMP_SENSOR_TYPE.setEnabled((JCB_OPTIONAL_ADC.getSelectedIndex() == TEMPERATURE));
        JCB_TEMP_SENSOR_TYPE.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JCB_TEMP_SENSOR_TYPEItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel87, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel90, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(69, 69, 69))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel104, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel105, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel95, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                        .addGap(225, 225, 225))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel106, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel101)
                            .addComponent(CB_TEMP_ERR_MIN_LIM)
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addComponent(jLabel107, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(JCB_TEMP_SENSOR_TYPE, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addComponent(jLabelCoasterBrakeThreshld, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TF_COASTER_BRAKE_THRESHOLD, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addComponent(jLabel94, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(JCB_SOC_CALC, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addComponent(jLabel91, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(JCB_DATA_ON_STARTUP, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(TF_DELAY_MENU, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(TF_NUM_DATA_AUTO_DISPLAY, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel19Layout.createSequentialGroup()
                                        .addComponent(jLabel102)
                                        .addGap(10, 10, 10)
                                        .addComponent(TF_ADC_THROTTLE_MIN, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel109)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(TF_TEMP_MIN_LIM, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(TF_ADC_THROTTLE_MAX, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(TF_TEMP_MAX_LIM, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(JCB_OPTIONAL_ADC, javax.swing.GroupLayout.Alignment.TRAILING, 0, 172, Short.MAX_VALUE)
                                        .addComponent(JCB_THROTTLE_MODE, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(JCB_BRAKE_FEATURE, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(32, 32, 32))))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_NUM_DATA_AUTO_DISPLAY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel90))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_DELAY_MENU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel87))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel91)
                    .addComponent(JCB_DATA_ON_STARTUP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel94)
                    .addComponent(JCB_SOC_CALC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel101)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel95)
                    .addComponent(JCB_BRAKE_FEATURE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_COASTER_BRAKE_THRESHOLD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCoasterBrakeThreshld))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(JCB_OPTIONAL_ADC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel106)
                    .addComponent(JCB_THROTTLE_MODE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel102)
                    .addComponent(TF_ADC_THROTTLE_MIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel109)
                    .addComponent(TF_ADC_THROTTLE_MAX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel107)
                    .addComponent(JCB_TEMP_SENSOR_TYPE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CB_TEMP_ERR_MIN_LIM)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_TEMP_MIN_LIM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel104))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_TEMP_MAX_LIM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel105))
                .addContainerGap())
        );

        TF_MOTOR_BLOCK_ERPS.setText("20");
        TF_MOTOR_BLOCK_ERPS.setToolTipText("Value 10 to 30 (ERPS)");

        TF_MOTOR_BLOCK_CURR.setText("30");
        TF_MOTOR_BLOCK_CURR.setToolTipText("Value 1 to 5 (0.1 A)");

        jLabelMOTOR_BLOCK_CURR.setText("Motor blocked error - threshold current");

        jLabelMOTOR_BLOCK_ERPS.setText("Motor blocked error - threshold ERPS");

        jLabelTF_MOTOR_BLOCK_TIME.setText("Motor blocked error - threshold time");

        TF_MOTOR_BLOCK_TIME.setText("2");
        TF_MOTOR_BLOCK_TIME.setToolTipText("Value 1 to 10 (0.1 s)");

        jLabelTHROTTLE_ASSIST_MIN.setText("Throttle assist value   min");

        TF_ASSIST_THROTTLE_MIN.setText("0");
        TF_ASSIST_THROTTLE_MIN.setToolTipText("Value 0 to 100");
        TF_ASSIST_THROTTLE_MIN.setEnabled((JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE)&&((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR)||(JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)));

        jLabelTHROTTLE_ASSIST_MAX.setText("max");

        TF_ASSIST_THROTTLE_MAX.setText("255");
        TF_ASSIST_THROTTLE_MAX.setToolTipText("Value MIN to 255");
        TF_ASSIST_THROTTLE_MAX.setEnabled((JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE)&&((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR)||(JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabelTHROTTLE_ASSIST_MIN)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TF_ASSIST_THROTTLE_MIN, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTHROTTLE_ASSIST_MAX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TF_ASSIST_THROTTLE_MAX, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jLabelTF_MOTOR_BLOCK_TIME, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(TF_MOTOR_BLOCK_TIME, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabelMOTOR_BLOCK_CURR)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(TF_MOTOR_BLOCK_CURR, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabelMOTOR_BLOCK_ERPS, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(TF_MOTOR_BLOCK_ERPS, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(50, 50, 50))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(TF_MOTOR_BLOCK_CURR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelMOTOR_BLOCK_CURR)
                        .addComponent(TF_MOTOR_BLOCK_TIME, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelTF_MOTOR_BLOCK_TIME))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TF_ASSIST_THROTTLE_MIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_ASSIST_THROTTLE_MAX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelTHROTTLE_ASSIST_MAX)
                            .addComponent(jLabelTHROTTLE_ASSIST_MIN))
                        .addGap(3, 3, 3)))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_MOTOR_BLOCK_ERPS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMOTOR_BLOCK_ERPS))
                .addGap(11, 11, 11))
        );

        jTabbedPane1.addTab("Advanced settings", jPanel8);

        label1.setFont(new java.awt.Font("Ebrima", 0, 24)); // NOI18N
        label1.setText("TSDZ2 Parameter Configurator");

        expSet.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        expSet.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        expSet.setFocusCycleRoot(true);
        jScrollPane1.setViewportView(expSet);

        jLabel1.setText("Experimental Settings");

        provSet.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        provSet.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(provSet);

        jLabel2.setText("Proven Settings");

        BTN_COMPILE.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        BTN_COMPILE.setText("Compile & Flash");

        jLabel4.setText("Version (last commits)");

        LB_LAST_COMMIT.setText("<html>Last commit</html>");
        LB_LAST_COMMIT.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        BTN_COPY_FILE_INI.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        BTN_COPY_FILE_INI.setText("Copy from Experimental to Proven");
        BTN_COPY_FILE_INI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_COPY_FILE_INIActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel4)
                        .addComponent(jLabel1)
                        .addComponent(jScrollPane1)
                        .addComponent(jScrollPane2)
                        .addComponent(BTN_COMPILE, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                        .addComponent(LB_LAST_COMMIT, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
                    .addComponent(jLabel2)
                    .addComponent(BTN_COPY_FILE_INI, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(4, 4, 4)
                        .addComponent(LB_LAST_COMMIT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(4, 4, 4)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BTN_COPY_FILE_INI)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addGap(4, 4, 4)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BTN_COMPILE, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("MotorConfiguration");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CB_WALK_TIME_ENAStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_WALK_TIME_ENAStateChanged
        TF_WALK_ASS_TIME.setEnabled(CB_WALK_TIME_ENA.isSelected() && CB_WALK_ASSIST_ENABLED.isSelected()&&(JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR));
    }//GEN-LAST:event_CB_WALK_TIME_ENAStateChanged

    private void TF_DATA_1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_DATA_1KeyReleased
        try {
            int index = Integer.parseInt(TF_DATA_1.getText());
            if ((index >= 0)&&(index <= 12)) {
                jLabelData1.setText("Data 1 - " + displayDataArray[index]); }
            else {
                jLabelData1.setText("Data 1");	}
        }
        catch(NumberFormatException ex){
            jLabelData1.setText("Data 1");
        }
    }//GEN-LAST:event_TF_DATA_1KeyReleased

    private void TF_DATA_2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_DATA_2KeyReleased
        try {
        int index = Integer.parseInt(TF_DATA_2.getText());
        if ((index >= 0)&&(index <= 12)) {
            jLabelData2.setText("Data 2 - " + displayDataArray[index]); }
        else {
            jLabelData2.setText("Data 2");	}
        }
        catch(NumberFormatException ex){
            jLabelData2.setText("Data 2");
        }
    }//GEN-LAST:event_TF_DATA_2KeyReleased

    private void TF_DATA_3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_DATA_3KeyReleased
        try {
        int index = Integer.parseInt(TF_DATA_3.getText());
        if ((index >= 0)&&(index <= 12)) {
            jLabelData3.setText("Data 3 - " + displayDataArray[index]); }
        else {
            jLabelData3.setText("Data 3");	}
        }
        catch(NumberFormatException ex){
            jLabelData3.setText("Data 3");
        }
    }//GEN-LAST:event_TF_DATA_3KeyReleased

    private void TF_DATA_4KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_DATA_4KeyReleased
        try {
        int index = Integer.parseInt(TF_DATA_4.getText());
        if ((index >= 0)&&(index <= 12)) {
            jLabelData4.setText("Data 4 - " + displayDataArray[index]); }
        else {
            jLabelData4.setText("Data 4");	}
        }
        catch(NumberFormatException ex){
            jLabelData4.setText("Data 4");
        }
    }//GEN-LAST:event_TF_DATA_4KeyReleased

    private void TF_DATA_5KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_DATA_5KeyReleased
        try {
        int index = Integer.parseInt(TF_DATA_5.getText());
        if ((index >= 0)&&(index <= 12)) {
            jLabelData5.setText("Data 5 - " + displayDataArray[index]); }
        else {
            jLabelData5.setText("Data 5");	}
        }
        catch(NumberFormatException ex){
            jLabelData5.setText("Data 5");
        }
    }//GEN-LAST:event_TF_DATA_5KeyReleased

    private void TF_DATA_6KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_DATA_6KeyReleased
        try {
        int index = Integer.parseInt(TF_DATA_6.getText());
        if ((index >= 0)&&(index <= 12)) {
            jLabelData6.setText("Data 6 - " + displayDataArray[index]); }
        else {
            jLabelData6.setText("Data 6");	}
        }
        catch(NumberFormatException ex){
            jLabelData6.setText("Data 6");
        }
    }//GEN-LAST:event_TF_DATA_6KeyReleased

    private void CB_STREET_POWER_LIMStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_STREET_POWER_LIMStateChanged
        TF_STREET_POWER_LIM.setEnabled(CB_STREET_POWER_LIM.isSelected());
    }//GEN-LAST:event_CB_STREET_POWER_LIMStateChanged

    private void CB_ASS_WITHOUT_PEDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_ASS_WITHOUT_PEDStateChanged
        TF_ASS_WITHOUT_PED_THRES.setEnabled(CB_ASS_WITHOUT_PED.isSelected());
    }//GEN-LAST:event_CB_ASS_WITHOUT_PEDStateChanged

    private void CB_WALK_ASSIST_ENABLEDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_WALK_ASSIST_ENABLEDStateChanged
        if (JCB_BRAKE_FEATURE.getSelectedIndex() == COASTER_BRAKE) {
            CB_WALK_ASSIST_ENABLED.setSelected(false);
        }
        CB_WALK_ASSIST_ENABLED.setEnabled(!boolCoasterBrake);
           
        TF_WALK_ASS_SPEED_1.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_2.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_3.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_4.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_SPEED_LIMIT.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
        CB_WALK_TIME_ENA.setEnabled(((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR) || (JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)) && CB_WALK_ASSIST_ENABLED.isSelected());
        TF_WALK_ASS_TIME.setEnabled(((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR) || (JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)) && CB_WALK_ASSIST_ENABLED.isSelected() && CB_WALK_TIME_ENA.isSelected());
        CB_STREET_WALK.setEnabled(CB_WALK_ASSIST_ENABLED.isSelected());
    }//GEN-LAST:event_CB_WALK_ASSIST_ENABLEDStateChanged

    private void CB_AUTO_DISPLAY_DATAStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_AUTO_DISPLAY_DATAStateChanged
        TF_NUM_DATA_AUTO_DISPLAY.setEnabled(CB_AUTO_DISPLAY_DATA.isSelected());
    }//GEN-LAST:event_CB_AUTO_DISPLAY_DATAStateChanged

    private void TF_STREET_SPEED_LIMKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_STREET_SPEED_LIMKeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intStreetSpeed = Integer.parseInt(TF_STREET_SPEED_LIM.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intStreetSpeed = Integer.parseInt(TF_STREET_SPEED_LIM.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_STREET_SPEED_LIMKeyReleased

    private void TF_NUM_DATA_AUTO_DISPLAYKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_NUM_DATA_AUTO_DISPLAYKeyReleased
        if (Integer.parseInt(TF_NUM_DATA_AUTO_DISPLAY.getText()) > 6) {
            TF_NUM_DATA_AUTO_DISPLAY.setText("6");
        }
        if (Integer.parseInt(TF_NUM_DATA_AUTO_DISPLAY.getText()) == 0) {
            TF_NUM_DATA_AUTO_DISPLAY.setText("1");
        }
    }//GEN-LAST:event_TF_NUM_DATA_AUTO_DISPLAYKeyReleased

    private void TF_WALK_ASS_SPEED_LIMITKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_WALK_ASS_SPEED_LIMITKeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intWalkSpeedLimit = Integer.parseInt(TF_WALK_ASS_SPEED_LIMIT.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intWalkSpeedLimit = Integer.parseInt(TF_WALK_ASS_SPEED_LIMIT.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_WALK_ASS_SPEED_LIMITKeyReleased

    private void TF_CRUISE_ASS_1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_CRUISE_ASS_1KeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intCruiseSpeed1 = Integer.parseInt(TF_CRUISE_ASS_1.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intCruiseSpeed1 = Integer.parseInt(TF_CRUISE_ASS_1.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_CRUISE_ASS_1KeyReleased

    private void TF_CRUISE_ASS_2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_CRUISE_ASS_2KeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intCruiseSpeed2 = Integer.parseInt(TF_CRUISE_ASS_2.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intCruiseSpeed2 = Integer.parseInt(TF_CRUISE_ASS_2.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_CRUISE_ASS_2KeyReleased

    private void TF_CRUISE_ASS_3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_CRUISE_ASS_3KeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intCruiseSpeed3 = Integer.parseInt(TF_CRUISE_ASS_3.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intCruiseSpeed3 = Integer.parseInt(TF_CRUISE_ASS_3.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_CRUISE_ASS_3KeyReleased

    private void TF_CRUISE_ASS_4KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_CRUISE_ASS_4KeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intCruiseSpeed4 = Integer.parseInt(TF_CRUISE_ASS_4.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intCruiseSpeed4 = Integer.parseInt(TF_CRUISE_ASS_4.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_CRUISE_ASS_4KeyReleased

    private void TF_CRUISE_SPEED_ENAKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_CRUISE_SPEED_ENAKeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intCruiseSpeed = Integer.parseInt(TF_CRUISE_SPEED_ENA.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intCruiseSpeed = Integer.parseInt(TF_CRUISE_SPEED_ENA.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_CRUISE_SPEED_ENAKeyReleased

    private void CB_FIELD_WEAKENING_ENABLEDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_FIELD_WEAKENING_ENABLEDStateChanged
    /*    if (CB_FIELD_WEAKENING_ENABLED.isSelected()) {
            CB_FIELD_WEAKENING_ENABLED.setText("Field weakening enabled - PWM 18.0 kHz"); }
        else {
            CB_FIELD_WEAKENING_ENABLED.setText("Field weakening disabled - PWM 15.6 kHz"); }
    */
    }//GEN-LAST:event_CB_FIELD_WEAKENING_ENABLEDStateChanged

    private void TF_WALK_ASS_SPEED_1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_WALK_ASS_SPEED_1KeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intWalkSpeed1 = Integer.parseInt(TF_WALK_ASS_SPEED_1.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intWalkSpeed1 = Integer.parseInt(TF_WALK_ASS_SPEED_1.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_WALK_ASS_SPEED_1KeyReleased

    private void TF_WALK_ASS_SPEED_2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_WALK_ASS_SPEED_2KeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intWalkSpeed2 = Integer.parseInt(TF_WALK_ASS_SPEED_2.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intWalkSpeed2 = Integer.parseInt(TF_WALK_ASS_SPEED_2.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_WALK_ASS_SPEED_2KeyReleased

    private void TF_WALK_ASS_SPEED_3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_WALK_ASS_SPEED_3KeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intWalkSpeed3 = Integer.parseInt(TF_WALK_ASS_SPEED_3.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intWalkSpeed3 = Integer.parseInt(TF_WALK_ASS_SPEED_3.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_WALK_ASS_SPEED_3KeyReleased

    private void TF_WALK_ASS_SPEED_4KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_WALK_ASS_SPEED_4KeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intWalkSpeed4 = Integer.parseInt(TF_WALK_ASS_SPEED_4.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intWalkSpeed4 = Integer.parseInt(TF_WALK_ASS_SPEED_4.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_WALK_ASS_SPEED_4KeyReleased

    private void CB_LIGHTSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_LIGHTSStateChanged
        TF_LIGHT_MODE_ON_START.setEnabled(CB_LIGHTS.isSelected());
        TF_LIGHT_MODE_1.setEnabled(CB_LIGHTS.isSelected());
        TF_LIGHT_MODE_2.setEnabled(CB_LIGHTS.isSelected());
        TF_LIGHT_MODE_3.setEnabled(CB_LIGHTS.isSelected());
    }//GEN-LAST:event_CB_LIGHTSStateChanged

    private void TF_LIGHT_MODE_3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_LIGHT_MODE_3KeyReleased
        try {
            int index = Integer.parseInt(TF_LIGHT_MODE_3.getText());
            if (((index >= 0)&&(index <= 8))||(index == 10)) {
                jLabelLights3.setText("<html>Mode 3 - " + lightModeArray[Integer.parseInt(TF_LIGHT_MODE_3.getText())] + "</html>"); }
            else {
                jLabelLights3.setText("Mode 3");	}
        }
        catch(NumberFormatException ex){
            jLabelLights3.setText("Mode 3");
        }
    }//GEN-LAST:event_TF_LIGHT_MODE_3KeyReleased

    private void TF_LIGHT_MODE_2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_LIGHT_MODE_2KeyReleased
        try {
            int index = Integer.parseInt(TF_LIGHT_MODE_2.getText());
            if ((index >= 0)&&(index <= 9)) {
                jLabelLights2.setText("<html>Mode 2 - " + lightModeArray[Integer.parseInt(TF_LIGHT_MODE_2.getText())] + "</html>"); }
            else {
                jLabelLights2.setText("Mode 2");	}
        }
        catch(NumberFormatException ex){
            jLabelLights2.setText("Mode 2");
        }
    }//GEN-LAST:event_TF_LIGHT_MODE_2KeyReleased

    private void TF_LIGHT_MODE_1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_LIGHT_MODE_1KeyReleased
        try {
            int index = Integer.parseInt(TF_LIGHT_MODE_1.getText());
            if ((index >= 0)&&(index <= 8)) {
                jLabelLights1.setText("<html>Mode 1 - " + lightModeArray[Integer.parseInt(TF_LIGHT_MODE_1.getText())] + "</html>"); }
            else {
                jLabelLights1.setText("Mode 1");	}
        }
        catch(NumberFormatException ex){
            jLabelLights1.setText("Mode 1");
        }
    }//GEN-LAST:event_TF_LIGHT_MODE_1KeyReleased

    private void TF_LIGHT_MODE_ON_STARTKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_LIGHT_MODE_ON_STARTKeyReleased
        try {
            int index = Integer.parseInt(TF_LIGHT_MODE_ON_START.getText());
            if ((index >= 0)&&(index <= 8)) {
                jLabelLights0.setText("<html>Lights mode on startup " + lightModeArray[Integer.parseInt(TF_LIGHT_MODE_ON_START.getText())] + "</html>"); }
            else {
                jLabelLights0.setText("Lights mode on startup");	}
        }
        catch(NumberFormatException ex){
            jLabelLights0.setText("Lights mode on startup");
        }
    }//GEN-LAST:event_TF_LIGHT_MODE_ON_STARTKeyReleased

    private void CB_MAX_SPEED_DISPLAYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_MAX_SPEED_DISPLAYStateChanged
        TF_MAX_SPEED.setEnabled(!CB_MAX_SPEED_DISPLAY.isSelected());
    }//GEN-LAST:event_CB_MAX_SPEED_DISPLAYStateChanged

    private void TF_MAX_SPEEDKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_MAX_SPEEDKeyReleased
        if (JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS) {
            intMaxSpeed = Integer.parseInt(TF_MAX_SPEED.getText());
        }
        else if ((JCB_UNITS_TYPE.getSelectedIndex() == MILES)||(JCB_UNITS_TYPE.getSelectedIndex() == ALTERNATIVE_MILES)) {
            intMaxSpeed = Integer.parseInt(TF_MAX_SPEED.getText()) * 16 / 10;
        }
    }//GEN-LAST:event_TF_MAX_SPEEDKeyReleased

    private void TF_TORQ_ADC_ANGLE_ADJKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_TORQ_ADC_ANGLE_ADJKeyReleased
        int value;
        int length = TF_TORQ_ADC_ANGLE_ADJ.getText().length();
        String sign = TF_TORQ_ADC_ANGLE_ADJ.getText().substring(0, 1);
        String empty = TF_TORQ_ADC_ANGLE_ADJ.getText();

        if (sign.equals("-")) {
            value = Integer.parseInt(TF_TORQ_ADC_ANGLE_ADJ.getText().substring(1, length)); }
        else if (empty.equals("")) {
            value = 0; }
        else {
            value = Integer.parseInt(TF_TORQ_ADC_ANGLE_ADJ.getText()); }

        if ((value >= 0)&&(value <= MIDDLE_ANGLE_ADJ)) {
            if (empty.equals("")) {
                intTorqueAngleAdj = MIDDLE_ANGLE_ADJ; }
            else if (sign.equals("-")) {
                intTorqueAngleAdj = MIDDLE_ANGLE_ADJ - value; }
            else {
                intTorqueAngleAdj = MIDDLE_ANGLE_ADJ + value; }
        }
        else {
            TF_TORQ_ADC_ANGLE_ADJ.setText("0");
            intTorqueAngleAdj = MIDDLE_ANGLE_ADJ;
        }
    }//GEN-LAST:event_TF_TORQ_ADC_ANGLE_ADJKeyReleased

    private void TF_TORQ_ADC_OFFSETKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_TORQ_ADC_OFFSETKeyReleased
        if(CB_ADC_STEP_ESTIM.isSelected()) {
            intTorqueAdcOffset = Integer.parseInt(TF_TORQ_ADC_OFFSET.getText());
            intTorqueAdcMax = Integer.parseInt(TF_TORQUE_ADC_MAX.getText());
            intTorqueAdcOnWeight = intTorqueAdcOffset + ((intTorqueAdcMax - intTorqueAdcOffset) * 75) / 100;
            intTorqueAdcStepCalc = (WEIGHT_ON_PEDAL * 167) / (intTorqueAdcOnWeight - intTorqueAdcOffset);
            TF_TORQ_PER_ADC_STEP.setText(String.valueOf(intTorqueAdcStepCalc));
        }
    }//GEN-LAST:event_TF_TORQ_ADC_OFFSETKeyReleased

    private void CB_ADC_STEP_ESTIMStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_ADC_STEP_ESTIMStateChanged
        TF_TORQ_PER_ADC_STEP.setEnabled(!CB_ADC_STEP_ESTIM.isSelected());

        if(CB_ADC_STEP_ESTIM.isSelected()) {
            intTorqueAdcOffset = Integer.parseInt(TF_TORQ_ADC_OFFSET.getText());
            intTorqueAdcMax = Integer.parseInt(TF_TORQUE_ADC_MAX.getText());
            intTorqueAdcOnWeight = intTorqueAdcOffset + ((intTorqueAdcMax - intTorqueAdcOffset) * 75) / 100;
            intTorqueAdcStepCalc = (WEIGHT_ON_PEDAL * 167) / (intTorqueAdcOnWeight - intTorqueAdcOffset);
            TF_TORQ_PER_ADC_STEP.setText(String.valueOf(intTorqueAdcStepCalc));
        }
        else {
            TF_TORQ_PER_ADC_STEP.setText(String.valueOf(intTorqueAdcStep));
        }
    }//GEN-LAST:event_CB_ADC_STEP_ESTIMStateChanged

    private void TF_TORQUE_ADC_MAXKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_TORQUE_ADC_MAXKeyReleased
        if(CB_ADC_STEP_ESTIM.isSelected()) {
            intTorqueAdcOffset = Integer.parseInt(TF_TORQ_ADC_OFFSET.getText());
            intTorqueAdcMax = Integer.parseInt(TF_TORQUE_ADC_MAX.getText());
            intTorqueAdcOnWeight = intTorqueAdcOffset + ((intTorqueAdcMax - intTorqueAdcOffset) * 75) / 100;
            intTorqueAdcStepCalc = (WEIGHT_ON_PEDAL * 167) / (intTorqueAdcOnWeight - intTorqueAdcOffset);
            TF_TORQ_PER_ADC_STEP.setText(String.valueOf(intTorqueAdcStepCalc));
        }
    }//GEN-LAST:event_TF_TORQUE_ADC_MAXKeyReleased

    private void TF_TORQ_ADC_RANGE_ADJKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_TORQ_ADC_RANGE_ADJKeyReleased
        int value;
        int length = TF_TORQ_ADC_RANGE_ADJ.getText().length();
        String sign = TF_TORQ_ADC_RANGE_ADJ.getText().substring(0, 1);
        String empty = TF_TORQ_ADC_RANGE_ADJ.getText();

        if (sign.equals("-")) {
            value = Integer.parseInt(TF_TORQ_ADC_RANGE_ADJ.getText().substring(1, length)); }
        else if (empty.equals("")) {
            value = 0; }
        else {
            value = Integer.parseInt(TF_TORQ_ADC_RANGE_ADJ.getText()); }

        if ((value >= 0)&&(value <= MIDDLE_RANGE_ADJ)) {
            if (empty.equals("")) {
                intTorqueRangeAdj = MIDDLE_RANGE_ADJ; }
            else if (sign.equals("-")) {
                intTorqueRangeAdj = MIDDLE_RANGE_ADJ - value; }
            else {
                intTorqueRangeAdj = MIDDLE_RANGE_ADJ + value; }
        }
        else {
            TF_TORQ_ADC_RANGE_ADJ.setText("0");
            intTorqueRangeAdj = MIDDLE_RANGE_ADJ;
        }
    }//GEN-LAST:event_TF_TORQ_ADC_RANGE_ADJKeyReleased

    private void TF_TORQ_ADC_OFFSET_ADJKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_TORQ_ADC_OFFSET_ADJKeyReleased
        int value;
        int length = TF_TORQ_ADC_OFFSET_ADJ.getText().length();
        String sign = TF_TORQ_ADC_OFFSET_ADJ.getText().substring(0, 1);
        String empty = TF_TORQ_ADC_OFFSET_ADJ.getText();

        if (sign.equals("-")) {
            value = Integer.parseInt(TF_TORQ_ADC_OFFSET_ADJ.getText().substring(1, length)); }
        else if (empty.equals("")) {
            value = 0; }
        else {
            value = Integer.parseInt(TF_TORQ_ADC_OFFSET_ADJ.getText()); }

        if ((value >= 0)&&(value <= MIDDLE_OFFSET_ADJ)) {
            if (empty.equals("")) {
                intTorqueOffsetAdj = MIDDLE_OFFSET_ADJ; }
            else if (sign.equals("-")) {
                intTorqueOffsetAdj = MIDDLE_OFFSET_ADJ - value; }
            else {
                intTorqueOffsetAdj = MIDDLE_OFFSET_ADJ + value;
                if (intTorqueOffsetAdj > OFFSET_MAX_VALUE) {
                    intTorqueOffsetAdj = OFFSET_MAX_VALUE;
                    TF_TORQ_ADC_OFFSET_ADJ.setText(String.valueOf(OFFSET_MAX_VALUE - MIDDLE_OFFSET_ADJ));
                }
            }
        }
        else {
            TF_TORQ_ADC_OFFSET_ADJ.setText("0");
            intTorqueOffsetAdj = MIDDLE_OFFSET_ADJ;
        }
    }//GEN-LAST:event_TF_TORQ_ADC_OFFSET_ADJKeyReleased

    private void CB_TORQUE_CALIBRATIONStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_TORQUE_CALIBRATIONStateChanged
        TF_TORQ_ADC_OFFSET.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        TF_TORQ_ADC_RANGE_ADJ.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        TF_TORQ_ADC_ANGLE_ADJ.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        TF_TORQUE_ADC_MAX.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        CB_ADC_STEP_ESTIM.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        TF_TORQ_PER_ADC_STEP_ADV.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
        CB_TOR_SENSOR_ADV.setEnabled(CB_TORQUE_CALIBRATION.isSelected());
    }//GEN-LAST:event_CB_TORQUE_CALIBRATIONStateChanged

    private void JCB_OPTIONAL_ADCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_OPTIONAL_ADCItemStateChanged
        boolOptionalAdcDisabled = false;
        boolOptionalAdcThrottle = false;
        boolOptionalAdcTemperature = false;
            
        switch (JCB_OPTIONAL_ADC.getSelectedIndex()) {
            case DISABLED:
                boolOptionalAdcDisabled = true;
                break;
            case THROTTLE:
                boolOptionalAdcThrottle = true;
                break;
            case TEMPERATURE:
                boolOptionalAdcTemperature = true;
                break;
            default:
                break;
        }

        TF_TEMP_MIN_LIM.setEnabled(boolOptionalAdcTemperature);
        TF_TEMP_MAX_LIM.setEnabled(boolOptionalAdcTemperature);
        CB_TEMP_ERR_MIN_LIM.setEnabled(boolOptionalAdcTemperature);
        JCB_TEMP_SENSOR_TYPE.setEnabled(boolOptionalAdcTemperature);
        
        if ((JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE)&&((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR)||(JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH))) {
            TF_ADC_THROTTLE_MIN.setEnabled(boolOptionalAdcThrottle);
            TF_ADC_THROTTLE_MAX.setEnabled(boolOptionalAdcThrottle);
            TF_ASSIST_THROTTLE_MIN.setEnabled(boolOptionalAdcThrottle);
            TF_ASSIST_THROTTLE_MAX.setEnabled(boolOptionalAdcThrottle);
            JCB_THROTTLE_MODE.setEnabled(boolOptionalAdcThrottle);
            JCB_THROTTLE_MODE_STREET.setEnabled(boolOptionalAdcThrottle);
        }
        else {
            TF_ADC_THROTTLE_MIN.setEnabled(false);
            TF_ADC_THROTTLE_MAX.setEnabled(false);
            TF_ASSIST_THROTTLE_MIN.setEnabled(false);
            TF_ASSIST_THROTTLE_MAX.setEnabled(false);
            JCB_THROTTLE_MODE.setEnabled(false);
            JCB_THROTTLE_MODE_STREET.setEnabled(false);
            JCB_THROTTLE_MODE.setSelectedIndex(DISABLED);
            JCB_THROTTLE_MODE_STREET.setSelectedIndex(DISABLED);
            if (JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE) {
                JCB_OPTIONAL_ADC.setSelectedIndex(DISABLED);
            }
        }
        
    }//GEN-LAST:event_JCB_OPTIONAL_ADCItemStateChanged

    private void JCB_DATA_ON_STARTUPItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_DATA_ON_STARTUPItemStateChanged
        boolStartupNone = false;
        boolStartupSoc = false;
        boolStartupVolts = false;
            
        switch (JCB_DATA_ON_STARTUP.getSelectedIndex()) {
            case NONE:
                boolStartupNone = true;
                break;
            case SOC:
                boolStartupSoc = true;
                break;
            case VOLTS:
                boolStartupVolts = true;
                break;
            default:
                break;
        }
    }//GEN-LAST:event_JCB_DATA_ON_STARTUPItemStateChanged

    private void JCB_SOC_CALCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_SOC_CALCItemStateChanged
            boolSocAuto = false;
            boolSocWh = false;
            boolSocVolts = false;
            
        switch (JCB_SOC_CALC.getSelectedIndex()) {
            case AUTO:
                boolSocAuto = true;
                break;
            case WH:
                boolSocWh = true;
                break;
            case VOLTS:
                boolSocVolts = true;
                break;
            default:
                break;
        }
    }//GEN-LAST:event_JCB_SOC_CALCItemStateChanged

    private void JCB_BRAKE_FEATUREItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_BRAKE_FEATUREItemStateChanged
       boolBrakeDisabled = false;
       boolBrakeSensor = false;
       boolCoasterBrake = false;
       boolTemperatureSwitch = false;
            
        switch (JCB_BRAKE_FEATURE.getSelectedIndex()) {
            case DISABLED:
                boolBrakeDisabled = true;
                break;
            case BRAKE_SENSOR:
                boolBrakeSensor = true;
                break;
            case COASTER_BRAKE:
                boolCoasterBrake = true;
                break;
            case TEMPERATURE_SWITCH:
                boolTemperatureSwitch = true;
                break;
            default:
                break;
        }
        
        if (JCB_BRAKE_FEATURE.getSelectedIndex() == COASTER_BRAKE) {
            CB_STARTUP_ASSIST_ENABLED.setSelected(false);
            CB_WALK_ASSIST_ENABLED.setSelected(false);
            CB_CRUISE_WHITOUT_PEDALING.setSelected(false);
        }
        
        CB_STARTUP_ASSIST_ENABLED.setEnabled(!boolCoasterBrake);
        CB_WALK_ASSIST_ENABLED.setEnabled(!boolCoasterBrake);
        TF_COASTER_BRAKE_THRESHOLD.setEnabled(boolCoasterBrake);
        TF_WALK_ASS_TIME.setEnabled(boolBrakeSensor || boolTemperatureSwitch);
        CB_WALK_TIME_ENA.setEnabled(boolBrakeSensor || boolTemperatureSwitch);
        CB_CRUISE_WHITOUT_PEDALING.setEnabled(boolBrakeSensor || boolTemperatureSwitch);
        
        if ((JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE)&&((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR)||(JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH))) {
            TF_ADC_THROTTLE_MIN.setEnabled(boolOptionalAdcThrottle);
            TF_ADC_THROTTLE_MAX.setEnabled(boolOptionalAdcThrottle);
            TF_ASSIST_THROTTLE_MIN.setEnabled(boolOptionalAdcThrottle);
            TF_ASSIST_THROTTLE_MAX.setEnabled(boolOptionalAdcThrottle);
            JCB_THROTTLE_MODE.setEnabled(boolOptionalAdcThrottle);
            JCB_THROTTLE_MODE_STREET.setEnabled(boolOptionalAdcThrottle);
        }
        else {
            TF_ADC_THROTTLE_MIN.setEnabled(false);
            TF_ADC_THROTTLE_MAX.setEnabled(false);
            TF_ASSIST_THROTTLE_MIN.setEnabled(false);
            TF_ASSIST_THROTTLE_MAX.setEnabled(false);
            JCB_THROTTLE_MODE.setEnabled(false);
            JCB_THROTTLE_MODE_STREET.setEnabled(false);
            JCB_THROTTLE_MODE.setSelectedIndex(DISABLED);
            JCB_THROTTLE_MODE_STREET.setSelectedIndex(DISABLED);
            if (JCB_OPTIONAL_ADC.getSelectedIndex() == THROTTLE) {
                JCB_OPTIONAL_ADC.setSelectedIndex(DISABLED);
            }
        }
    }//GEN-LAST:event_JCB_BRAKE_FEATUREItemStateChanged

    private void JCB_THROTTLE_MODEItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_THROTTLE_MODEItemStateChanged
            if (JCB_THROTTLE_MODE.getSelectedIndex() < JCB_THROTTLE_MODE_STREET.getSelectedIndex()) {
                JCB_THROTTLE_MODE_STREET.setSelectedIndex(JCB_THROTTLE_MODE.getSelectedIndex());
            }
    }//GEN-LAST:event_JCB_THROTTLE_MODEItemStateChanged

    private void JCB_THROTTLE_MODE_STREETItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_THROTTLE_MODE_STREETItemStateChanged
            if (JCB_THROTTLE_MODE_STREET.getSelectedIndex() > JCB_THROTTLE_MODE.getSelectedIndex()) {
                JCB_THROTTLE_MODE_STREET.setSelectedIndex(JCB_THROTTLE_MODE.getSelectedIndex());
            }
    }//GEN-LAST:event_JCB_THROTTLE_MODE_STREETItemStateChanged

    private void JCB_ASSIST_MODE_ON_STARTUPItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_ASSIST_MODE_ON_STARTUPItemStateChanged
        boolAssistStartupPower = false;
        boolAssistStartupTorque = false;
        boolAssistStartupCadence = false;
        boolAssistStartupEmtb = false;
        boolAssistStartupHybrid = false;
            
        switch (JCB_ASSIST_MODE_ON_STARTUP.getSelectedIndex()) {
            case POWER:
                boolAssistStartupPower = true;
                break;
            case TORQUE:
                boolAssistStartupTorque = true;
                break;
            case CADENCE:
                boolAssistStartupCadence = true;
                break;
            case EMTB:
                boolAssistStartupEmtb = true;
                break;
            case HYBRID:
                boolAssistStartupHybrid = true;
                break;
            default:
                break;
        }
    }//GEN-LAST:event_JCB_ASSIST_MODE_ON_STARTUPItemStateChanged

    private void JCB_DISPLAY_TYPEItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_DISPLAY_TYPEItemStateChanged
        boolDisplayTypeVLCD5 = false;
        boolDisplayTypeVLCD6 = false;
        boolDisplayTypeXH18 = false;
        boolDisplayType850C = false;
            
        switch (JCB_DISPLAY_TYPE.getSelectedIndex()) {
            case VLCD5:
                boolDisplayTypeVLCD5 = true;
                break;
            case VLCD6:
                boolDisplayTypeVLCD6 = true;
                break;
            case XH18:
                boolDisplayTypeXH18 = true;
                break;
            case C850:
                boolDisplayType850C = true;
                break;
            default:
                break;
        }
        
        if ((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD6)&&(JCB_UNITS_TYPE.getSelectedIndex() == MILES)) {
            JCB_UNITS_TYPE.setSelectedIndex(ALTERNATIVE_MILES);
        }
        
        if ((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD5)||(JCB_DISPLAY_TYPE.getSelectedIndex() == C850)) {
            TF_BAT_CELL_5_6.setEnabled(true);
            TF_BAT_CELL_4_6.setEnabled(true);
            TF_BAT_CELL_3_6.setEnabled(true);
            TF_BAT_CELL_2_6.setEnabled(true);
            TF_BAT_CELL_1_6.setEnabled(true);
            TF_BAT_CELL_3_4.setEnabled(false);
            TF_BAT_CELL_2_4.setEnabled(false);
            TF_BAT_CELL_1_4.setEnabled(false);
        }
        else {
            TF_BAT_CELL_5_6.setEnabled(false);
            TF_BAT_CELL_4_6.setEnabled(false);
            TF_BAT_CELL_3_6.setEnabled(false);
            TF_BAT_CELL_2_6.setEnabled(false);
            TF_BAT_CELL_1_6.setEnabled(false);
            TF_BAT_CELL_3_4.setEnabled(true);
            TF_BAT_CELL_2_4.setEnabled(true);
            TF_BAT_CELL_1_4.setEnabled(true);
        }
    }//GEN-LAST:event_JCB_DISPLAY_TYPEItemStateChanged

    private void CB_CRUISE_ENABLEDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_CRUISE_ENABLEDStateChanged
        TF_CRUISE_ASS_1.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_ASS_2.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_ASS_3.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_ASS_4.setEnabled(CB_CRUISE_ENABLED.isSelected());
        TF_CRUISE_SPEED_ENA.setEnabled(CB_CRUISE_ENABLED.isSelected());
        CB_STREET_CRUISE.setEnabled(CB_CRUISE_ENABLED.isSelected());
        CB_CRUISE_WHITOUT_PEDALING.setEnabled(((JCB_BRAKE_FEATURE.getSelectedIndex() == BRAKE_SENSOR) || (JCB_BRAKE_FEATURE.getSelectedIndex() == TEMPERATURE_SWITCH)) && CB_CRUISE_ENABLED.isSelected());
    }//GEN-LAST:event_CB_CRUISE_ENABLEDStateChanged

    private void CB_STARTUP_BOOST_ON_STARTStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_STARTUP_BOOST_ON_STARTStateChanged
        TF_BOOST_TORQUE_FACTOR.setEnabled(CB_STARTUP_BOOST_ON_START.isSelected());
        TF_BOOST_CADENCE_STEP.setEnabled(CB_STARTUP_BOOST_ON_START.isSelected());
        RB_BOOST_AT_ZERO_CADENCE.setEnabled(CB_STARTUP_BOOST_ON_START.isSelected());
        RB_BOOST_AT_ZERO_SPEED.setEnabled(CB_STARTUP_BOOST_ON_START.isSelected());
    }//GEN-LAST:event_CB_STARTUP_BOOST_ON_STARTStateChanged

    private void CB_SMOOTH_START_ENABLEDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_SMOOTH_START_ENABLEDStateChanged
        TF_SMOOTH_START_RAMP.setEnabled(CB_SMOOTH_START_ENABLED.isSelected());
    }//GEN-LAST:event_CB_SMOOTH_START_ENABLEDStateChanged

    private void JCB_TEMP_SENSOR_TYPEItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_TEMP_SENSOR_TYPEItemStateChanged
        if (JCB_TEMP_SENSOR_TYPE.getSelectedIndex() == LM35) {
            boolTemperatureSensorType = false;
        }
        else {
            boolTemperatureSensorType = true;
        }
    }//GEN-LAST:event_JCB_TEMP_SENSOR_TYPEItemStateChanged

    private void TF_EMTB_ASS_1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_TF_EMTB_ASS_1FocusLost
        if ((TF_EMTB_ASS_1.getText().isEmpty())||(TF_EMTB_ASS_1.getText().isBlank())) {
            TF_EMTB_ASS_1.setText(String.valueOf(EMTB_ASSIST_MIN));
        }
        else if (Integer.parseInt(TF_EMTB_ASS_1.getText()) < EMTB_ASSIST_MIN) {
            TF_EMTB_ASS_1.setText(String.valueOf(EMTB_ASSIST_MIN));
        }
    }//GEN-LAST:event_TF_EMTB_ASS_1FocusLost

    private void TF_EMTB_ASS_2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_TF_EMTB_ASS_2FocusLost
        if ((TF_EMTB_ASS_2.getText().isEmpty())||(TF_EMTB_ASS_2.getText().isBlank())) {
            TF_EMTB_ASS_2.setText(String.valueOf(EMTB_ASSIST_MIN));
        }
        else if (Integer.parseInt(TF_EMTB_ASS_2.getText()) < EMTB_ASSIST_MIN) {
            TF_EMTB_ASS_2.setText(String.valueOf(EMTB_ASSIST_MIN));
        }
    }//GEN-LAST:event_TF_EMTB_ASS_2FocusLost

    private void TF_EMTB_ASS_3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_TF_EMTB_ASS_3FocusLost
        if ((TF_EMTB_ASS_3.getText().isEmpty())||(TF_EMTB_ASS_3.getText().isBlank())) {
            TF_EMTB_ASS_3.setText(String.valueOf(EMTB_ASSIST_MIN));
        }
        else if (Integer.parseInt(TF_EMTB_ASS_3.getText()) < EMTB_ASSIST_MIN) {
            TF_EMTB_ASS_3.setText(String.valueOf(EMTB_ASSIST_MIN));
        }
    }//GEN-LAST:event_TF_EMTB_ASS_3FocusLost

    private void TF_EMTB_ASS_4FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_TF_EMTB_ASS_4FocusLost
        if ((TF_EMTB_ASS_4.getText().isEmpty())||(TF_EMTB_ASS_4.getText().isBlank())) {
            TF_EMTB_ASS_4.setText(String.valueOf(EMTB_ASSIST_MIN));
        }
        else if (Integer.parseInt(TF_EMTB_ASS_4.getText()) < EMTB_ASSIST_MIN) {
            TF_EMTB_ASS_4.setText(String.valueOf(EMTB_ASSIST_MIN));
        }
    }//GEN-LAST:event_TF_EMTB_ASS_4FocusLost

    private void CB_STARTUP_ASSIST_ENABLEDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_STARTUP_ASSIST_ENABLEDStateChanged
        if (JCB_BRAKE_FEATURE.getSelectedIndex() == COASTER_BRAKE) {
            CB_STARTUP_ASSIST_ENABLED.setSelected(false);
        }
        CB_STARTUP_ASSIST_ENABLED.setEnabled(!boolCoasterBrake);
    }//GEN-LAST:event_CB_STARTUP_ASSIST_ENABLEDStateChanged

    private void BTN_COPY_FILE_INIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_COPY_FILE_INIActionPerformed
        int selectedIndex = expSet.getSelectedIndex();
        experimentalSettingsList.setSelectedIndex(selectedIndex);
        strFileSource = experimentalSettingsList.getSelectedValue().toString();
        
        UIManager.put("OptionPane.inputDialogTitle", "Copy file " + strFileSource);
        
        if (provSet.getSelectedIndex() >= 0) {
            strFileDest = JOptionPane.showInputDialog(null, "New file name (.ini)", provSet.getSelectedValue());
        }
        else {
            strFileDest = JOptionPane.showInputDialog(null, "New file name (.ini)", "My_settings.ini");
        }
        
        if ((strFileDest != null) && (strFileDest.length() > 0)) {
            if ((strFileDest.length() > 4)&&(strFileDest.substring(strFileDest.length() - 4).toLowerCase().equals(".ini"))) {
                // extension ok
            }
            else {
                strFileDest = strFileDest + ".ini";
            }
            
            File FileSource = new File(experimentalSettingsDir + File.separator + strFileSource);
            File FileDest = new File(provenSettingsDir + File.separator + strFileDest);
            
            try {
                if (FileDest.exists() && !FileDest.isDirectory()) {
                    int reply = JOptionPane.showConfirmDialog(null, strFileDest + " file already exists, overwrite?", "Copy file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (reply == JOptionPane.YES_OPTION) {
                        Files.copy(FileSource.toPath(), FileDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                else {
                    Files.copy(FileSource.toPath(), FileDest.toPath());
                    provenSettingsFilesModel.add(0, new FileContainer(FileDest));
                    expSet.setSelectedIndex(0);
                    provSet.repaint();
                }
            } catch (IOException ex) {
                Logger.getLogger(TSDZ2_Configurator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_BTN_COPY_FILE_INIActionPerformed

    private void CB_CRUISE_WHITOUT_PEDALINGStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CB_CRUISE_WHITOUT_PEDALINGStateChanged
        if (JCB_BRAKE_FEATURE.getSelectedIndex() == COASTER_BRAKE) {
            CB_CRUISE_WHITOUT_PEDALING.setSelected(false);
        }
    }//GEN-LAST:event_CB_CRUISE_WHITOUT_PEDALINGStateChanged

    private void JCB_UNITS_TYPEItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JCB_UNITS_TYPEItemStateChanged
        boolUnitsKilometers = false;
        boolUnitsMiles = false;
        boolAlternativeMiles = false;
        
        switch (JCB_UNITS_TYPE.getSelectedIndex()) {
            case KILOMETERS:
                boolUnitsKilometers = true;
                break;
            case MILES:
                boolUnitsMiles = true;
                break;
            case ALTERNATIVE_MILES:
                boolAlternativeMiles = true;
                break;
            default:
                break;
        }
        
        if ((JCB_DISPLAY_TYPE.getSelectedIndex() == VLCD6)&&(JCB_UNITS_TYPE.getSelectedIndex() == MILES)) {
            JCB_UNITS_TYPE.setSelectedIndex(ALTERNATIVE_MILES);
        }
        
        if ((JCB_UNITS_TYPE.getSelectedIndex() == KILOMETERS)) {
            jLabelMaxSpeed.setText("Max speed offroad mode (km/h)");
            jLabelStreetSpeed.setText("Street speed limit (km/h)");
            jLabelCruiseSpeedUnits.setText("km/h");
            jLabelWalkSpeedUnits.setText("km/h x10");
            TF_MAX_SPEED.setText(String.valueOf(intMaxSpeed));
            TF_STREET_SPEED_LIM.setText(String.valueOf(intStreetSpeed));
            TF_WALK_ASS_SPEED_1.setText(String.valueOf(intWalkSpeed1));
            TF_WALK_ASS_SPEED_2.setText(String.valueOf(intWalkSpeed2));
            TF_WALK_ASS_SPEED_3.setText(String.valueOf(intWalkSpeed3));
            TF_WALK_ASS_SPEED_4.setText(String.valueOf(intWalkSpeed4));
            TF_WALK_ASS_SPEED_LIMIT.setText(String.valueOf(intWalkSpeedLimit));
            TF_CRUISE_SPEED_ENA.setText(String.valueOf(intCruiseSpeed));
            TF_CRUISE_ASS_1.setText(String.valueOf(intCruiseSpeed1));
            TF_CRUISE_ASS_2.setText(String.valueOf(intCruiseSpeed2));
            TF_CRUISE_ASS_3.setText(String.valueOf(intCruiseSpeed3));
            TF_CRUISE_ASS_4.setText(String.valueOf(intCruiseSpeed4));
        }
        else {
            jLabelMaxSpeed.setText("Max speed offroad mode (mph)");
            jLabelStreetSpeed.setText("Street speed limit (mph)");
            jLabelCruiseSpeedUnits.setText("mph");
            jLabelWalkSpeedUnits.setText("mph x10");
            TF_MAX_SPEED.setText(String.valueOf((intMaxSpeed * 10 + 5) / 16));
            TF_STREET_SPEED_LIM.setText(String.valueOf((intStreetSpeed * 10 + 5) / 16));
            TF_WALK_ASS_SPEED_1.setText(String.valueOf((intWalkSpeed1 * 10 + 5) / 16));
            TF_WALK_ASS_SPEED_2.setText(String.valueOf((intWalkSpeed2 * 10 + 5) / 16));
            TF_WALK_ASS_SPEED_3.setText(String.valueOf((intWalkSpeed3 * 10 + 5) / 16));
            TF_WALK_ASS_SPEED_4.setText(String.valueOf((intWalkSpeed4 * 10 + 5) / 16));
            TF_WALK_ASS_SPEED_LIMIT.setText(String.valueOf((intWalkSpeedLimit * 10 + 5) / 16));
            TF_CRUISE_SPEED_ENA.setText(String.valueOf((intCruiseSpeed * 10 + 5) / 16));
            TF_CRUISE_ASS_1.setText(String.valueOf((intCruiseSpeed1 * 10 + 5) / 16));
            TF_CRUISE_ASS_2.setText(String.valueOf((intCruiseSpeed2 * 10 + 5) / 16));
            TF_CRUISE_ASS_3.setText(String.valueOf((intCruiseSpeed3 * 10 + 5) / 16));
            TF_CRUISE_ASS_4.setText(String.valueOf((intCruiseSpeed4 * 10 + 5) / 16));
        }
    }//GEN-LAST:event_JCB_UNITS_TYPEItemStateChanged

    /*
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TSDZ2_Configurator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TSDZ2_Configurator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TSDZ2_Configurator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TSDZ2_Configurator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TSDZ2_Configurator().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BTN_COMPILE;
    private javax.swing.JButton BTN_COPY_FILE_INI;
    private javax.swing.JCheckBox CB_ADC_STEP_ESTIM;
    private javax.swing.JCheckBox CB_ASS_WITHOUT_PED;
    private javax.swing.JCheckBox CB_AUTO_DISPLAY_DATA;
    private javax.swing.JCheckBox CB_CRUISE_ENABLED;
    private javax.swing.JCheckBox CB_CRUISE_WHITOUT_PEDALING;
    private javax.swing.JCheckBox CB_FIELD_WEAKENING_ENABLED;
    private javax.swing.JCheckBox CB_LIGHTS;
    private javax.swing.JCheckBox CB_MAX_SPEED_DISPLAY;
    private javax.swing.JCheckBox CB_ODO_COMPENSATION;
    private javax.swing.JCheckBox CB_SET_PARAM_ON_START;
    private javax.swing.JCheckBox CB_SMOOTH_START_ENABLED;
    private javax.swing.JCheckBox CB_STARTUP_ASSIST_ENABLED;
    private javax.swing.JCheckBox CB_STARTUP_BOOST_ON_START;
    private javax.swing.JCheckBox CB_STREET_CRUISE;
    private javax.swing.JCheckBox CB_STREET_MODE_ON_START;
    private javax.swing.JCheckBox CB_STREET_POWER_LIM;
    private javax.swing.JCheckBox CB_STREET_WALK;
    private javax.swing.JCheckBox CB_TEMP_ERR_MIN_LIM;
    private javax.swing.JCheckBox CB_TORQUE_CALIBRATION;
    private javax.swing.JCheckBox CB_TOR_SENSOR_ADV;
    private javax.swing.JCheckBox CB_WALK_ASSIST_ENABLED;
    private javax.swing.JCheckBox CB_WALK_TIME_ENA;
    private javax.swing.JComboBox<String> JCB_ASSIST_MODE_ON_STARTUP;
    private javax.swing.JComboBox<String> JCB_BRAKE_FEATURE;
    private javax.swing.JComboBox<String> JCB_DATA_ON_STARTUP;
    private javax.swing.JComboBox<String> JCB_DISPLAY_TYPE;
    private javax.swing.JComboBox<String> JCB_OPTIONAL_ADC;
    private javax.swing.JComboBox<String> JCB_SOC_CALC;
    private javax.swing.JComboBox<String> JCB_TEMP_SENSOR_TYPE;
    private javax.swing.JComboBox<String> JCB_THROTTLE_MODE;
    private javax.swing.JComboBox<String> JCB_THROTTLE_MODE_STREET;
    private javax.swing.JComboBox<String> JCB_UNITS_TYPE;
    private javax.swing.JLabel LB_LAST_COMMIT;
    private javax.swing.JLabel Label_Parameter1;
    private javax.swing.JLabel Label_Parameter2;
    private javax.swing.JLabel Label_Parameter3;
    private javax.swing.JLabel Label_Parameter5;
    private javax.swing.JRadioButton RB_BOOST_AT_ZERO_CADENCE;
    private javax.swing.JRadioButton RB_BOOST_AT_ZERO_SPEED;
    private javax.swing.JRadioButton RB_DISPLAY_ALWAY_ON;
    private javax.swing.JRadioButton RB_DISPLAY_WORK_ON;
    private javax.swing.JRadioButton RB_MOTOR_36V;
    private javax.swing.JRadioButton RB_MOTOR_48V;
    private javax.swing.JRadioButton RB_eMTB_POWER;
    private javax.swing.JRadioButton RB_eMTB_TORQUE;
    private javax.swing.JTextField TF_ADC_THROTTLE_MAX;
    private javax.swing.JTextField TF_ADC_THROTTLE_MIN;
    private javax.swing.JTextField TF_ASSIST_THROTTLE_MAX;
    private javax.swing.JTextField TF_ASSIST_THROTTLE_MIN;
    private javax.swing.JTextField TF_ASS_LEVELS_1_OF_5;
    private javax.swing.JTextField TF_ASS_WITHOUT_PED_THRES;
    private javax.swing.JTextField TF_BATT_CAPACITY;
    private javax.swing.JTextField TF_BATT_CAPACITY_CAL;
    private javax.swing.JTextField TF_BATT_NUM_CELLS;
    private javax.swing.JTextField TF_BATT_POW_MAX;
    private javax.swing.JTextField TF_BATT_VOLT_CAL;
    private javax.swing.JTextField TF_BATT_VOLT_CUT_OFF;
    private javax.swing.JTextField TF_BAT_CELL_1_4;
    private javax.swing.JTextField TF_BAT_CELL_1_6;
    private javax.swing.JTextField TF_BAT_CELL_2_4;
    private javax.swing.JTextField TF_BAT_CELL_2_6;
    private javax.swing.JTextField TF_BAT_CELL_3_4;
    private javax.swing.JTextField TF_BAT_CELL_3_6;
    private javax.swing.JTextField TF_BAT_CELL_4_6;
    private javax.swing.JTextField TF_BAT_CELL_5_6;
    private javax.swing.JTextField TF_BAT_CELL_EMPTY;
    private javax.swing.JTextField TF_BAT_CELL_FULL;
    private javax.swing.JTextField TF_BAT_CELL_OVER;
    private javax.swing.JTextField TF_BAT_CELL_SOC;
    private javax.swing.JTextField TF_BAT_CUR_MAX;
    private javax.swing.JTextField TF_BOOST_CADENCE_STEP;
    private javax.swing.JTextField TF_BOOST_TORQUE_FACTOR;
    private javax.swing.JTextField TF_CADENCE_ASS_1;
    private javax.swing.JTextField TF_CADENCE_ASS_2;
    private javax.swing.JTextField TF_CADENCE_ASS_3;
    private javax.swing.JTextField TF_CADENCE_ASS_4;
    private javax.swing.JTextField TF_COASTER_BRAKE_THRESHOLD;
    private javax.swing.JTextField TF_CRUISE_ASS_1;
    private javax.swing.JTextField TF_CRUISE_ASS_2;
    private javax.swing.JTextField TF_CRUISE_ASS_3;
    private javax.swing.JTextField TF_CRUISE_ASS_4;
    private javax.swing.JTextField TF_CRUISE_SPEED_ENA;
    private javax.swing.JTextField TF_DATA_1;
    private javax.swing.JTextField TF_DATA_2;
    private javax.swing.JTextField TF_DATA_3;
    private javax.swing.JTextField TF_DATA_4;
    private javax.swing.JTextField TF_DATA_5;
    private javax.swing.JTextField TF_DATA_6;
    private javax.swing.JTextField TF_DELAY_DATA_1;
    private javax.swing.JTextField TF_DELAY_DATA_2;
    private javax.swing.JTextField TF_DELAY_DATA_3;
    private javax.swing.JTextField TF_DELAY_DATA_4;
    private javax.swing.JTextField TF_DELAY_DATA_5;
    private javax.swing.JTextField TF_DELAY_DATA_6;
    private javax.swing.JTextField TF_DELAY_MENU;
    private javax.swing.JTextField TF_EMTB_ASS_1;
    private javax.swing.JTextField TF_EMTB_ASS_2;
    private javax.swing.JTextField TF_EMTB_ASS_3;
    private javax.swing.JTextField TF_EMTB_ASS_4;
    private javax.swing.JTextField TF_LIGHT_MODE_1;
    private javax.swing.JTextField TF_LIGHT_MODE_2;
    private javax.swing.JTextField TF_LIGHT_MODE_3;
    private javax.swing.JTextField TF_LIGHT_MODE_ON_START;
    private javax.swing.JTextField TF_MAX_SPEED;
    private javax.swing.JTextField TF_MOTOR_ACC;
    private javax.swing.JTextField TF_MOTOR_BLOCK_CURR;
    private javax.swing.JTextField TF_MOTOR_BLOCK_ERPS;
    private javax.swing.JTextField TF_MOTOR_BLOCK_TIME;
    private javax.swing.JTextField TF_MOTOR_DEC;
    private javax.swing.JTextField TF_NUM_DATA_AUTO_DISPLAY;
    private javax.swing.JTextField TF_POWER_ASS_1;
    private javax.swing.JTextField TF_POWER_ASS_2;
    private javax.swing.JTextField TF_POWER_ASS_3;
    private javax.swing.JTextField TF_POWER_ASS_4;
    private javax.swing.JTextField TF_SMOOTH_START_RAMP;
    private javax.swing.JTextField TF_STREET_POWER_LIM;
    private javax.swing.JTextField TF_STREET_SPEED_LIM;
    private javax.swing.JTextField TF_TEMP_MAX_LIM;
    private javax.swing.JTextField TF_TEMP_MIN_LIM;
    private javax.swing.JTextField TF_TORQUE_ADC_MAX;
    private javax.swing.JTextField TF_TORQUE_ASS_1;
    private javax.swing.JTextField TF_TORQUE_ASS_2;
    private javax.swing.JTextField TF_TORQUE_ASS_3;
    private javax.swing.JTextField TF_TORQUE_ASS_4;
    private javax.swing.JTextField TF_TORQ_ADC_ANGLE_ADJ;
    private javax.swing.JTextField TF_TORQ_ADC_OFFSET;
    private javax.swing.JTextField TF_TORQ_ADC_OFFSET_ADJ;
    private javax.swing.JTextField TF_TORQ_ADC_RANGE_ADJ;
    private javax.swing.JTextField TF_TORQ_PER_ADC_STEP;
    private javax.swing.JTextField TF_TORQ_PER_ADC_STEP_ADV;
    private javax.swing.JTextField TF_WALK_ASS_SPEED_1;
    private javax.swing.JTextField TF_WALK_ASS_SPEED_2;
    private javax.swing.JTextField TF_WALK_ASS_SPEED_3;
    private javax.swing.JTextField TF_WALK_ASS_SPEED_4;
    private javax.swing.JTextField TF_WALK_ASS_SPEED_LIMIT;
    private javax.swing.JTextField TF_WALK_ASS_TIME;
    private javax.swing.JTextField TF_WHEEL_CIRCUMF;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.ButtonGroup buttonGroup9;
    private javax.swing.JList<String> expSet;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel105;
    private javax.swing.JLabel jLabel106;
    private javax.swing.JLabel jLabel107;
    private javax.swing.JLabel jLabel109;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JLabel jLabelCoasterBrakeThreshld;
    private javax.swing.JLabel jLabelCruiseSpeedUnits;
    private javax.swing.JLabel jLabelData1;
    private javax.swing.JLabel jLabelData2;
    private javax.swing.JLabel jLabelData3;
    private javax.swing.JLabel jLabelData4;
    private javax.swing.JLabel jLabelData5;
    private javax.swing.JLabel jLabelData6;
    private javax.swing.JLabel jLabelLights0;
    private javax.swing.JLabel jLabelLights1;
    private javax.swing.JLabel jLabelLights2;
    private javax.swing.JLabel jLabelLights3;
    private javax.swing.JLabel jLabelMOTOR_BLOCK_CURR;
    private javax.swing.JLabel jLabelMOTOR_BLOCK_ERPS;
    private javax.swing.JLabel jLabelMaxSpeed;
    private javax.swing.JLabel jLabelMotorFastStop;
    private javax.swing.JLabel jLabelStreetSpeed;
    private javax.swing.JLabel jLabelTF_MOTOR_BLOCK_TIME;
    private javax.swing.JLabel jLabelTHROTTLE_ASSIST_MAX;
    private javax.swing.JLabel jLabelTHROTTLE_ASSIST_MIN;
    private javax.swing.JLabel jLabelWalkSpeed;
    private javax.swing.JLabel jLabelWalkSpeedUnits;
    private javax.swing.JLabel jLabel_TORQ_ADC_MAX;
    private javax.swing.JLabel jLabel_TORQ_ADC_OFFSET;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private java.awt.Label label1;
    private javax.swing.JList<String> provSet;
    // End of variables declaration//GEN-END:variables
}
