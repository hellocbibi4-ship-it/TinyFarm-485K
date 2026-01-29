             # 🚜 TinyFarm - Project Phase 1: Interactive Dashboard

This folder contains the **Frontend Mockup** for the TinyFarm project. It serves as a functional demonstration of the game's interface, using real-time data loading and a dynamic UI inspired by our Figma designs.

## 📁 Folder Structure

```text
/screens
│
├── index.html          # Main entry point (HTML5)
├── /css
│   └── style.css       # Layout, Glassmorphism UI, and Background Blur
├── /js
│   └── script.js       # Logic to fetch data and render animals
├── /data
│   └── farmData.json   # Mocked data (Cash, Inventory, Animal list)
└── /assets             # Background (PNG) and Animal Icons (SVG)

```

## 🚀 Key Features

* **Data Separation**: The application uses a strictly decoupled architecture. All farm statistics and animal data are stored in a standalone `JSON` file, simulating a database response.
* **Dynamic Rendering**: The JavaScript engine fetches the `farmData.json` and populates the dashboard automatically. Adding a new animal to the JSON file will instantly display it on the screen.
* **Modern UI Design**:
* **Background Focus**: A high-fidelity export from Figma serves as the background.
* **Glassmorphism**: Resources (Cash, Water, Food) are displayed in a blurred, translucent header for high readability.
* **Responsive Grid**: Animals are displayed in a flexible grid system that adapts to the screen size.



## 🛠️ How to Launch

Because the project uses the JavaScript `fetch()` API to load local data, you cannot open the `index.html` file directly by double-clicking it. You must use a local web server to avoid **CORS** security restrictions.

1. Open the project in **VS Code**.
2. Ensure the **Live Server** extension is installed.
3. Right-click `index.html` and select **"Open with Live Server"**.
4. The dashboard will be available at `http://127.0.0.1:5500/screens/index.html`.

## 🎨 Design Assets

* **Background**: Custom-designed environment exported from Figma.
* **Icons**: Vector-based SVGs (Cow, Chicken, Rabbit) for perfect scaling.

---