package kt.firmata.hardware

data class HumidityChanged(override val hardware: Hygrometer) : HygrometerEvent
