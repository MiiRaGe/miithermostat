import board
import digitalio
import time

from settings import ERROR_LED

led = digitalio.DigitalInOut(board.LED)
led.direction = digitalio.Direction.OUTPUT

error_led = digitalio.DigitalInOut(ERROR_LED)
error_led.direction = digitalio.Direction.OUTPUT

def blink(times=1):
    for _ in range(0, times):
        led.value = True
        time.sleep(1)
        led.value = False
        time.sleep(1)

def set_error_led(value = True):
    error_led.value = value