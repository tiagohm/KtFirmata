#include <Arduino.h>
#include <ConfigurableFirmata.h>

#include <DigitalInputFirmata.h>
DigitalInputFirmata digitalInput;

#include <DigitalOutputFirmata.h>
DigitalOutputFirmata digitalOutput;

#include <AnalogInputFirmata.h>
AnalogInputFirmata analogInput;

#include <AnalogOutputFirmata.h>
AnalogOutputFirmata analogOutput;

#include <Wire.h>
#include <I2CFirmata.h>
I2CFirmata i2c;

#include <OneWireFirmata.h>
OneWireFirmata oneWire;

#include <DhtFirmata.h>
DhtFirmata dhtFirmata;

#include <FirmataExt.h>
FirmataExt firmataExt;

#include <FirmataReporting.h>
FirmataReporting reporting;

void systemResetCallback()
{
    firmataExt.reset();
}

void initTransport()
{
    Firmata.begin(115200);
}

void initFirmata()
{
    firmataExt.addFeature(digitalInput);
    firmataExt.addFeature(digitalOutput);

    firmataExt.addFeature(analogInput);
    firmataExt.addFeature(analogOutput);

    firmataExt.addFeature(i2c);

    firmataExt.addFeature(oneWire);
    firmataExt.addFeature(dhtFirmata);

    firmataExt.addFeature(reporting);

    Firmata.attach(SYSTEM_RESET, systemResetCallback);
}

void setup()
{
    // Do this before initTransport(), because some client libraries expect that a reset sends this automatically.
    Firmata.setFirmwareNameAndVersion("ConfigurableFirmata", FIRMATA_FIRMWARE_MAJOR_VERSION, FIRMATA_FIRMWARE_MINOR_VERSION);
    initTransport();
    initFirmata();

    Firmata.parse(SYSTEM_RESET);
}

void loop()
{
    while (Firmata.available())
    {
        Firmata.processInput();

        if (!Firmata.isParsingMessage())
        {
            break;
        }
    }

    firmataExt.report(reporting.elapsed());
}
