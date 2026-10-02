package com.example.math

data class UnitCategory(
    val id: String,
    val name: String,
    val units: List<UnitDefinition>
)

data class UnitDefinition(
    val symbol: String,
    val name: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

object UnitConversionEngine {

    val categories: List<UnitCategory> = listOf(
        UnitCategory(
            id = "length",
            name = "Length",
            units = listOf(
                UnitDefinition("m", "Meter", { it }, { it }),
                UnitDefinition("km", "Kilometer", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("cm", "Centimeter", { it * 0.01 }, { it * 100.0 }),
                UnitDefinition("mm", "Millimeter", { it * 0.001 }, { it * 1000.0 }),
                UnitDefinition("µm", "Micrometer", { it * 1e-6 }, { it * 1e6 }),
                UnitDefinition("nm", "Nanometer", { it * 1e-9 }, { it * 1e9 }),
                UnitDefinition("in", "Inch", { it * 0.0254 }, { it / 0.0254 }),
                UnitDefinition("ft", "Foot", { it * 0.3048 }, { it / 0.3048 }),
                UnitDefinition("yd", "Yard", { it * 0.9144 }, { it / 0.9144 }),
                UnitDefinition("mi", "Mile", { it * 1609.344 }, { it / 1609.344 }),
                UnitDefinition("nmi", "Nautical Mile", { it * 1852.0 }, { it / 1852.0 }),
                UnitDefinition("ly", "Light Year", { it * 9.4607e15 }, { it / 9.4607e15 })
            )
        ),
        UnitCategory(
            id = "mass",
            name = "Mass & Weight",
            units = listOf(
                UnitDefinition("kg", "Kilogram", { it }, { it }),
                UnitDefinition("g", "Gram", { it * 0.001 }, { it * 1000.0 }),
                UnitDefinition("mg", "Milligram", { it * 1e-6 }, { it * 1e6 }),
                UnitDefinition("t", "Metric Ton", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("lb", "Pound", { it * 0.45359237 }, { it / 0.45359237 }),
                UnitDefinition("oz", "Ounce", { it * 0.028349523125 }, { it / 0.028349523125 }),
                UnitDefinition("st", "Stone", { it * 6.35029318 }, { it / 6.35029318 })
            )
        ),
        UnitCategory(
            id = "area",
            name = "Area",
            units = listOf(
                UnitDefinition("m²", "Square Meter", { it }, { it }),
                UnitDefinition("km²", "Square Kilometer", { it * 1e6 }, { it / 1e6 }),
                UnitDefinition("cm²", "Square Centimeter", { it * 1e-4 }, { it * 1e4 }),
                UnitDefinition("ha", "Hectare", { it * 10000.0 }, { it / 10000.0 }),
                UnitDefinition("ac", "Acre", { it * 4046.8564224 }, { it / 4046.8564224 }),
                UnitDefinition("ft²", "Square Foot", { it * 0.09290304 }, { it / 0.09290304 }),
                UnitDefinition("mi²", "Square Mile", { it * 2589988.110336 }, { it / 2589988.110336 })
            )
        ),
        UnitCategory(
            id = "volume",
            name = "Volume",
            units = listOf(
                UnitDefinition("L", "Liter", { it }, { it }),
                UnitDefinition("mL", "Milliliter", { it * 0.001 }, { it * 1000.0 }),
                UnitDefinition("m³", "Cubic Meter", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("cm³", "Cubic Centimeter", { it * 0.001 }, { it * 1000.0 }),
                UnitDefinition("gal", "Gallon (US)", { it * 3.785411784 }, { it / 3.785411784 }),
                UnitDefinition("qt", "Quart (US)", { it * 0.946352946 }, { it / 0.946352946 }),
                UnitDefinition("pt", "Pint (US)", { it * 0.473176473 }, { it / 0.473176473 }),
                UnitDefinition("cup", "Cup (US)", { it * 0.2365882365 }, { it / 0.2365882365 }),
                UnitDefinition("fl oz", "Fluid Ounce (US)", { it * 0.0295735295625 }, { it / 0.0295735295625 })
            )
        ),
        UnitCategory(
            id = "temperature",
            name = "Temperature",
            units = listOf(
                UnitDefinition("°C", "Celsius", { it }, { it }),
                UnitDefinition("°F", "Fahrenheit", { (it - 32.0) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32.0 }),
                UnitDefinition("K", "Kelvin", { it - 273.15 }, { it + 273.15 }),
                UnitDefinition("°R", "Rankine", { (it - 491.67) * 5.0 / 9.0 }, { (it + 273.15) * 9.0 / 5.0 })
            )
        ),
        UnitCategory(
            id = "speed",
            name = "Speed",
            units = listOf(
                UnitDefinition("m/s", "Meter per second", { it }, { it }),
                UnitDefinition("km/h", "Kilometer per hour", { it / 3.6 }, { it * 3.6 }),
                UnitDefinition("mph", "Mile per hour", { it * 0.44704 }, { it / 0.44704 }),
                UnitDefinition("kn", "Knot", { it * 0.514444 }, { it / 0.514444 }),
                UnitDefinition("c", "Speed of Light", { it * 299792458.0 }, { it / 299792458.0 })
            )
        ),
        UnitCategory(
            id = "pressure",
            name = "Pressure",
            units = listOf(
                UnitDefinition("Pa", "Pascal", { it }, { it }),
                UnitDefinition("kPa", "Kilopascal", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("bar", "Bar", { it * 1e5 }, { it / 1e5 }),
                UnitDefinition("psi", "Pounds per sq inch", { it * 6894.757293 }, { it / 6894.757293 }),
                UnitDefinition("atm", "Atmosphere", { it * 101325.0 }, { it / 101325.0 }),
                UnitDefinition("mmHg", "Torr / mmHg", { it * 133.322368 }, { it / 133.322368 })
            )
        ),
        UnitCategory(
            id = "energy",
            name = "Energy & Work",
            units = listOf(
                UnitDefinition("J", "Joule", { it }, { it }),
                UnitDefinition("kJ", "Kilojoule", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("cal", "Calorie", { it * 4.184 }, { it / 4.184 }),
                UnitDefinition("kcal", "Kilocalorie (Food)", { it * 4184.0 }, { it / 4184.0 }),
                UnitDefinition("Wh", "Watt-hour", { it * 3600.0 }, { it / 3600.0 }),
                UnitDefinition("kWh", "Kilowatt-hour", { it * 3.6e6 }, { it / 3.6e6 }),
                UnitDefinition("eV", "Electronvolt", { it * 1.602176634e-19 }, { it / 1.602176634e-19 }),
                UnitDefinition("BTU", "British Thermal Unit", { it * 1055.056 }, { it / 1055.056 })
            )
        ),
        UnitCategory(
            id = "power",
            name = "Power",
            units = listOf(
                UnitDefinition("W", "Watt", { it }, { it }),
                UnitDefinition("kW", "Kilowatt", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("MW", "Megawatt", { it * 1e6 }, { it / 1e6 }),
                UnitDefinition("hp", "Horsepower (mechanical)", { it * 745.699872 }, { it / 745.699872 })
            )
        ),
        UnitCategory(
            id = "time",
            name = "Time",
            units = listOf(
                UnitDefinition("s", "Second", { it }, { it }),
                UnitDefinition("ms", "Millisecond", { it * 0.001 }, { it * 1000.0 }),
                UnitDefinition("µs", "Microsecond", { it * 1e-6 }, { it * 1e6 }),
                UnitDefinition("min", "Minute", { it * 60.0 }, { it / 60.0 }),
                UnitDefinition("h", "Hour", { it * 3600.0 }, { it / 3600.0 }),
                UnitDefinition("d", "Day", { it * 86400.0 }, { it / 86400.0 }),
                UnitDefinition("wk", "Week", { it * 604800.0 }, { it / 604800.0 }),
                UnitDefinition("yr", "Year (365 days)", { it * 31536000.0 }, { it / 31536000.0 })
            )
        ),
        UnitCategory(
            id = "data",
            name = "Digital Storage",
            units = listOf(
                UnitDefinition("B", "Byte", { it }, { it }),
                UnitDefinition("KB", "Kilobyte (1000 B)", { it * 1e3 }, { it / 1e3 }),
                UnitDefinition("KiB", "Kibibyte (1024 B)", { it * 1024.0 }, { it / 1024.0 }),
                UnitDefinition("MB", "Megabyte (10^6 B)", { it * 1e6 }, { it / 1e6 }),
                UnitDefinition("MiB", "Mebibyte (1024^2 B)", { it * 1048576.0 }, { it / 1048576.0 }),
                UnitDefinition("GB", "Gigabyte (10^9 B)", { it * 1e9 }, { it / 1e9 }),
                UnitDefinition("GiB", "Gibibyte (1024^3 B)", { it * 1073741824.0 }, { it / 1073741824.0 }),
                UnitDefinition("TB", "Terabyte (10^12 B)", { it * 1e12 }, { it / 1e12 })
            )
        )
    )

    fun convert(value: Double, fromUnit: UnitDefinition, toUnit: UnitDefinition): Double {
        val baseValue = fromUnit.toBase(value)
        return toUnit.fromBase(baseValue)
    }
}
