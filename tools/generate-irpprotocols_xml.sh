#!/bin/sh

# Generates IrpProtocols.xml from IrpProtocols.ini

java -jar target/IrpTransmogrifier-0.0.1dev-jar-with-dependencies.jar -i src/main/config/IrpProtocols.ini -o src/main/config/IrpProtocols.xml  writeconfig
