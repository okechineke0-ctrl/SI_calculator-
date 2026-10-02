# Ocean Math AI

A professional, production-ready scientific and graphing calculator Android application engineered by **Ocean Technologies**.

Designed for engineers, mathematicians, students, researchers, scientists, and educators who need deterministic mathematical precision combined with modern Gemini AI reasoning, graphing, and camera-based Snap & Solve.

---

## 🌟 Key Capabilities

### 1. High-Precision Deterministic Math Engine
- **Arithmetic & Operators**: Addition, subtraction, multiplication, division, full parentheses nesting, percentages, absolute value, factorials ($n!$).
- **Scientific Functions**: Trigonometry ($\sin, \cos, \tan, \cot, \sec, \csc$), Inverse ($\sin^{-1}, \cos^{-1}, \tan^{-1}$), Hyperbolic ($\sinh, \cosh, \tanh$), Logarithms ($\ln, \log_{10}, \log_2$), Exponentials ($e^x, 10^x, 2^x$), Powers & Arbitrary Roots ($\sqrt{x}, \sqrt[3]{x}, \sqrt[n]{x}$).
- **Angle Modes**: DEG, RAD, GRAD with dynamic live recalculation.
- **Notations & Formatting**: Standard, Scientific notation ($1.23 \times 10^5$), Engineering notation ($12.3\text{ k}$), and Fraction conversions.
- **Memory Storage**: Casio-standard $MC, MR, M+, M-, MS$ registers.

### 2. Interactive Graphing Module
- Multi-function plotting ($y_1, y_2, y_3$) with distinct color curves.
- Smooth gesture pan and pinch-to-zoom.
- Interactive crosshair coordinate tracer displaying dynamic $(x, y)$ values.
- Grid markings, axis labels, and origin lines.

### 3. Gemini AI Snap & Solve
- Photograph any mathematics problem or pick from gallery.
- Dual-stage OCR: Transcribes and displays the recognized equation first, allowing the user to inspect and edit before solving.
- Multiple Solution Modes:
  - **Step-by-Step**: Comprehensive pedagogical derivation.
  - **Answer Only**: Concise simplified result.
  - **Explain Like I'm Learning**: Intuitive foundational explanation.
  - **Show Formula**: Formal theorems, variable definitions, and substitutions.
  - **Check My Answer**: Domain verification and edge cases.
  - **Alternative Method**: Contrasting analytical, geometric, or numerical methods.
- **Offline Resilient**: Deterministic math runs 100% offline without network dependency.

### 4. Advanced Math Tools
- **Algebra & Equations**: Linear equations ($ax+b=c$), Quadratic equations ($ax^2+bx+c=0$) with real/complex roots and vertex coordinates, and 2x2 / 3x3 simultaneous linear systems.
- **Matrices & Vectors**: Up to 4x4 matrix Determinant, Trace, Inverse, Transpose, and Eigenvalues; 3D vector Dot Product, Cross Product, Angle, and Projection.
- **Statistics & Probability**: Mean, Median, Mode, Sample/Population Variance, Sample/Population Standard Deviation, Quartiles (Q1, Q3, IQR), Linear Regression ($y=mx+c, r, R^2$), and Binomial/Normal distributions.
- **Calculus & Series**: Numerical differentiation $f'(x)$ using 5-point stencil central difference, Composite Simpson's 1/3 Rule definite integration $\int_a^b f(x) dx$, and Tangent line equations.
- **Complex Numbers**: $z_1, z_2$ arithmetic, magnitude, argument, Polar form ($r \angle \theta$), and Euler form ($r e^{i\theta}$).
- **Number Theory**: GCD, LCM, Prime testing, Prime factorization, and Modular arithmetic.
- **Geometry**: 2D and 3D shapes (Triangle with Heron's formula, Circle, Rectangle, Cylinder, Cone, Sphere, Cuboid).
- **Physics**: Kinematics, Newton's Laws, Kinetic/Potential Energy, Work/Power, Ohm's Law, and Parallel Resistors.
- **Finance**: Simple Interest, Compound Interest, and Loan EMI Monthly Amortization.
- **Unit Converter**: Comprehensive categories (Length, Mass, Volume, Temperature, Speed, Pressure, Energy, Power, Time, Digital Data).

### 5. Calculation History & Formula Library
- Local SQLite persistence via Room Database with reactive Kotlin Flows.
- Searchable calculation history, favorite pinning, one-tap copy, and reuse in calculator.
- Curated Formula Library across 8 STEM categories with one-tap "Open in Calculator".

---

## 🛠️ Architecture & Clean Design

```
com.example/
├── MainActivity.kt                # Root entry point & M3 Navigation Scaffold
├── data/
│   ├── AppDatabase.kt             # Room Database singleton
│   ├── CalculationDao.kt          # Reactive Room DAO
│   ├── CalculationEntity.kt       # History persistence schema
│   ├── CalculationRepository.kt   # Repository pattern abstraction
│   └── AppPreferences.kt          # SharedPreferences settings state
├── math/
│   ├── MathEngine.kt              # Tokenizer, Shunting-Yard, RPN evaluator
│   ├── AlgebraEngine.kt           # Linear, quadratic, and matrix system solvers
│   ├── CalculusEngine.kt          # Numerical differentiation and Simpson integration
│   ├── MatrixEngine.kt            # Linear algebra & 3D vector calculations
│   ├── StatisticsEngine.kt        # Descriptive statistics & linear regression
│   ├── ComplexEngine.kt           # Complex number arithmetic and polar/Euler forms
│   ├── NumberTheoryEngine.kt      # GCD, LCM, primes, and modular arithmetic
│   ├── GeometryEngine.kt          # 2D & 3D geometric properties
│   ├── PhysicsEngine.kt           # Physics formulas with substitution breakdowns
│   ├── FinanceEngine.kt           # Loan EMI, compound interest, and taxes
│   ├── UnitConversionEngine.kt    # Bidirectional SI & imperial unit conversions
│   └── FormulaLibrary.kt          # Structured formula catalog
├── ai/
│   └── AIService.kt               # Gemini 3.5 Flash REST API & multimodal OCR
└── ui/
    ├── MainViewModel.kt           # MVVM StateFlow coordinator
    ├── theme/
    │   ├── Color.kt               # Ocean Navy & Cyan branding colors
    │   ├── Theme.kt               # Material 3 Light & Dark color schemes
    │   └── Type.kt                # Typography scale
    └── screens/
        ├── CalculatorScreen.kt    # Casio-inspired scientific calculator interface
        ├── GraphScreen.kt         # Custom Canvas interactive graphing plotter
        ├── AiSolveScreen.kt       # Camera snap, OCR, and AI solution modes
        ├── MathToolsScreen.kt     # 10 dedicated STEM mathematical tools
        ├── FormulasHistoryScreen.kt # Formula reference & Room history manager
        └── SettingsScreen.kt      # Preferences, API keys, and legal disclosures
```

---

## 🚀 Building & Running

### 1. Configure the Gemini API Key
Ocean Math AI reads its Gemini API key via `BuildConfig.GEMINI_API_KEY`:
1. In Google AI Studio, open the **Secrets Panel** and add `GEMINI_API_KEY`.
2. Alternatively, users can enter their own key in **Settings > AI Mathematics Assistant**.

### 2. Run Unit Tests
To execute automated unit tests verifying the mathematical engine:
```bash
gradle :app:testDebugUnitTest
```

### 3. Build APK
```bash
gradle :app:assembleDebug
```

### 4. Build Signed Android App Bundle (.aab) for Google Play
```bash
gradle :app:bundleRelease
```
The generated bundle will be located at:
`app/build/outputs/bundle/release/app-release.aab`

---

## 🔒 Privacy & Permissions
- **Camera (`CAMERA`)**: Declared with `android:required="false"`. Camera permission is only requested at runtime when the user explicitly triggers "Take Photo" in Snap & Solve.
- **Network (`INTERNET`, `ACCESS_NETWORK_STATE`)**: Used strictly for Gemini AI assistance when requested by the user.
- **Vibration (`VIBRATE`)**: Provides tactile haptic feedback on keypad button presses (can be toggled in Settings).

---

## 🏢 Publication Identity
- **Application Name**: Ocean Math AI
- **Developer**: Ocean Technologies
- **Application ID**: `com.aistudio.oceanmath.wqvfpt`
