document.addEventListener("DOMContentLoaded", () => {
    
    // --- Navigation Logic ---
    const navSearch = document.getElementById("nav-search");
    const navAdmin = document.getElementById("nav-admin");
    const navCart = document.getElementById("nav-cart");
    const navReports = document.getElementById("nav-reports"); // <-- Added this
    
    const viewSearch = document.getElementById("view-search");
    const viewAdmin = document.getElementById("view-admin");
    const viewCart = document.getElementById("view-cart");
    const viewReports = document.getElementById("view-reports"); // <-- Added this

    function switchTab(activeBtn, viewToShow) {
        // Clear all active states
        document.querySelectorAll(".nav-btn").forEach(btn => btn.classList.remove("active"));
        document.querySelectorAll(".tab-content").forEach(tab => tab.classList.remove("active-tab", "hidden"));
        
        // Hide all views explicitly
        viewSearch.classList.add("hidden");
        viewAdmin.classList.add("hidden");
        viewCart.classList.add("hidden");
        if (viewReports) viewReports.classList.add("hidden"); // <-- Added this

        // Set the active button and view
        activeBtn.classList.add("active");
        viewToShow.classList.remove("hidden");
        viewToShow.classList.add("active-tab");
    }

    // Event listeners for tabs
    navSearch.addEventListener("click", () => switchTab(navSearch, viewSearch));
    navAdmin.addEventListener("click", () => switchTab(navAdmin, viewAdmin));
    navCart.addEventListener("click", () => switchTab(navCart, viewCart));
    if (navReports) {
        navReports.addEventListener("click", () => switchTab(navReports, viewReports)); // <-- Added this
    }

    // Initialize to show search by default
    switchTab(navSearch, viewSearch);

    // --- Search Logic ---
    const searchBtn = document.getElementById("searchBtn");
    searchBtn.addEventListener("click", () => {
        const pId = document.getElementById("productIdInput").value.trim();
        const d = document.getElementById("dateInput").value;
        if (!pId) return showError("Please enter a Product ID.", true);
        fetchPrices(pId, d);
    });

    function fetchPrices(productId, date) {
        document.getElementById("resultsTable").classList.add("hidden");
        document.getElementById("loading").classList.remove("hidden");
        document.getElementById("error-message").classList.add("hidden");

        fetch(`http://localhost:8080/api/prices/${productId}?date=${date}`)
            .then(res => res.json())
            .then(data => {
                document.getElementById("loading").classList.add("hidden");
                if (data.length === 0) return showError("No data found.", true);
                
                const tbody = document.getElementById("tableBody");
                tbody.innerHTML = "";
                data.forEach(item => {
                    tbody.innerHTML += `<tr>
                        <td><strong>${item.store}</strong></td>
                        <td>${item.date}</td>
                        <td>${item.brand}</td>
                        <td>${item.category}</td>
                        <td>${item.price.toFixed(2)}</td>
                    </tr>`;
                });
                document.getElementById("resultsTable").classList.remove("hidden");
            })
            .catch(err => {
                document.getElementById("loading").classList.add("hidden");
                showError("Server error. Is it running?", true);
            });
    }

    // --- Admin CRUD Logic ---
    const actionSelect = document.getElementById("crudAction");
    const productFields = document.getElementById("productFields");
    
    // Hide name/price/cat inputs if we are just Deleting
    actionSelect.addEventListener("change", (e) => {
        if(e.target.value === "DELETE") {
            Array.from(productFields.querySelectorAll('input:not(#adminId)')).forEach(i => i.parentElement.classList.add('hidden'));
        } else {
            Array.from(productFields.querySelectorAll('.form-row')).forEach(r => r.classList.remove('hidden'));
        }
    });

    document.getElementById("submitAdminBtn").addEventListener("click", () => {
        const method = document.getElementById("crudAction").value; // POST, PUT, DELETE
        const store = document.getElementById("adminStore").value;
        const date = document.getElementById("adminDate").value;
        const id = document.getElementById("adminId").value.trim();
        
        if (!store || !date || !id) return showError("Store, Date, and Product ID are required.", false);

        let url = `http://localhost:8080/api/products?store=${store}&date=${date}`;
        if (method !== "POST") url = `http://localhost:8080/api/products/${id}?store=${store}&date=${date}`;

        const options = {
            method: method,
            headers: { "Content-Type": "application/json" }
        };

        // If creating or updating, pack the product body
        if (method !== "DELETE") {
            const productBody = {
                id: id,
                name: document.getElementById("adminName").value,
                category: document.getElementById("adminCategory").value,
                brand: document.getElementById("adminBrand").value,
                price: parseFloat(document.getElementById("adminPrice").value) || 0,
                unit: document.getElementById("adminUnit").value,
                quantity: parseFloat(document.getElementById("adminQuantity").value) || 1,
                currency: "RON",
                datePosted: date
            };
            options.body = JSON.stringify(productBody);
        }

        fetch(url, options)
            .then(res => res.text())
            .then(text => showMessage(text, 'msg-success', false))
            .catch(err => showMessage("Failed to execute action.", 'msg-error', false));
    });

    function showError(msg, isSearch) {
        showMessage(msg, 'msg-error', isSearch);
    }

    function showMessage(msg, className, isSearch) {
        const el = isSearch ? document.getElementById("error-message") : document.getElementById("adminMessage");
        el.textContent = msg;
        el.className = ` ${className}`;
        el.classList.remove("hidden");
        setTimeout(() => el.classList.add("hidden"), 4000);
    }

    // --- Shopping Cart Logic ---
    const cartListEl = document.getElementById("cartList");
    const optimizationResultEl = document.getElementById("optimizationResult");
    let shoppingCart = []; // stores IDs. e.g. ["P001", "P001", "P020"]

    // Add to Cart
    document.getElementById("addToCartBtn").addEventListener("click", () => {
        const id = document.getElementById("cartProductId").value.trim().toUpperCase();
        const qty = parseInt(document.getElementById("cartQuantity").value) || 1;
        
        if (!id) return alert("Enter a Product ID");

        for(let i=0; i<qty; i++) {
            shoppingCart.push(id);
        }
        renderCart();
        document.getElementById("cartProductId").value = ""; 
    });

    // Clear Cart
    document.getElementById("clearCartBtn").addEventListener("click", () => {
        shoppingCart = [];
        renderCart();
        optimizationResultEl.classList.add("hidden");
    });

    function renderCart() {
        cartListEl.innerHTML = "";
        // Group by quantity for display
        const counts = {};
        shoppingCart.forEach(id => counts[id] = (counts[id] || 0) + 1);
        
        for (const [id, count] of Object.entries(counts)) {
            cartListEl.innerHTML += `<li><span>${id}</span> <span>x${count}</span></li>`;
        }
    }

    // Send to Optimizer Endpoint
    document.getElementById("optimizeBtn").addEventListener("click", () => {
        if (shoppingCart.length === 0) return alert("Cart is empty");
        
        const date = document.getElementById("cartDate").value;
        const loading = document.getElementById("optimizationLoading");
        
        loading.classList.remove("hidden");
        optimizationResultEl.classList.add("hidden");

        fetch(`http://localhost:8080/api/optimize?date=${date}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(shoppingCart)
        })
        .then(res => res.text()) // Expecting raw text from the file
        .then(text => {
            loading.classList.add("hidden");
            optimizationResultEl.textContent = text;
            optimizationResultEl.classList.remove("hidden");
        })
        .catch(err => {
            loading.classList.add("hidden");
            alert("Optimization failed.");
        });
    });

    // --- Reports Logic ---
    document.getElementById("generateReportBtn").addEventListener("click", () => {
        const reportType = document.getElementById("reportType").value;
        const date = document.getElementById("reportDate").value;
        const store = document.getElementById("reportStore").value.trim();
        const limit = document.getElementById("reportLimit").value;
        
        const loading = document.getElementById("reportLoading");
        const resultContainer = document.getElementById("reportResult");
        
        loading.classList.remove("hidden");
        resultContainer.classList.add("hidden");

        // Build the query string dynamically based on what the user inputted
        let url = `http://localhost:8080/api/reports/${reportType}?date=${date}`;
        if (store) url += `&store=${encodeURIComponent(store)}`;
        if (limit && reportType === "best-discounts") url += `&limit=${encodeURIComponent(limit)}`;

        fetch(url)
            .then(res => res.json())
            .then(data => {
                loading.classList.add("hidden");
                
                if(data.length === 0) {
                    resultContainer.textContent = "No data found for this report.";
                } else {
                    resultContainer.textContent = data.join("\n");
                }
                
                resultContainer.classList.remove("hidden");
            })
            .catch(err => {
                loading.classList.add("hidden");
                alert("Failed to fetch the report. Check if server is running.");
            });
    });
});