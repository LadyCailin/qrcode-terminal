# QRCode Java Edition

> Going where no QRCode has gone before.

Ported from JavaScript to Java, based on the qrcode-terminal npm module
https://github.com/gtanner/qrcode-terminal

Can be easily embedded in Java console applications to print
QR Codes to the terminal. Usage is straightforward.

    String out = new QRTerminal().generate("String to encode");
    System.out.println(out);

On Windows terminals, however, due to interoperability issues with
terminals, printing to standard out with the large format doesn't
work. To solve this problem, you need to use jline library. Include
https://search.maven.org/artifact/jline/jline/2.14.6/jar and simply call

    org.fusesource.jansi.AnsiConsole.systemInstall();

before displaying the QR code to the terminal. Alternatively, you
may obtain the 2D boolean array and display it however you like.

    QRCode qrcode = new QRCode(-1, QRErrorCorrectLevel.L);
    qrcode.addData(input); // String to encode
    qrcode.make();
    Boolean[][] modules = qrcode.getModules();
