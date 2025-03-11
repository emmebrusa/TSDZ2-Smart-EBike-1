/* 
 * delay utilite for STM8 family
 * COSMIC and SDCC
 * Terentiev Oleg
 * t.oleg@ymail.com
 */

#ifndef UTIL_DELAY_H_
#define UTIL_DELAY_H_ 1

#include "stm8s.h"

#ifndef F_CPU
#error F_CPU is not defined!
#endif

// Amount of cycles was tuned to use desired amount time.
// Compiler settings may affect the delay time precision.

#define T_COUNT_US(x) ((( (x) * (F_CPU / 1000000UL) )-4U)*4/15U)
static inline void delay_cycl( unsigned short _ticks ){
	nop();
	nop();
	do { 			// ASM:  lab$: decw X; tnzw X; jrne lab$
        _ticks--;	//   1c;  +   2c  + ; 1/2c   + 1/4 (mystery) = 3.75 = 15/4
	} while ( _ticks );
	nop();
}


/**
 * Delays for a specified number of microseconds.
 * 
 * Blocking delay.
 *
 * @param _us the number of microseconds to delay, min = 4, max = 1000
 *
 * @return void
 *
 * @throws None
 */
static inline void delay_us( unsigned short _us ) {
	delay_cycl( (unsigned short)( T_COUNT_US(_us) ));
}

/**
 * Delays for a specified number of miliseconds.
 * Blocking delay
 *
 * @param _us the number of microseconds to delay
 *
 * @return void
 *
 * @throws None
 */
static inline void delay_ms( unsigned short _ms ) {
	while ( _ms-- ) {
		delay_us( 1000 );
	}
}

#endif

