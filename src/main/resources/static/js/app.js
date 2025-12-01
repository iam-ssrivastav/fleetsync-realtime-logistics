const stompClient = Stomp.over(new SockJS('/ws-fleet'));
const truckTableBody = document.getElementById('truck-table-body');
const alertsList = document.getElementById('alerts-list');
const truckDataMap = new Map(); // Store latest data for each truck
const truckMarkers = new Map(); // Store Leaflet markers

// Initialize Map
const map = L.map('map').setView([40.75, -73.98], 12); // NYC Center
L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
    subdomains: 'abcd',
    maxZoom: 20
}).addTo(map);

// Initialize Chart
const ctx = document.getElementById('speedChart').getContext('2d');
const speedChart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: [],
        datasets: [{
            label: 'Avg Fleet Speed',
            data: [],
            borderColor: '#38bdf8',
            tension: 0.4,
            borderWidth: 2,
            pointRadius: 0
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            y: { beginAtZero: true, max: 100, grid: { color: '#334155' } },
            x: { display: false }
        },
        plugins: { legend: { display: false } }
    }
});

// Connect to WebSocket
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    // Subscribe to Telemetry
    stompClient.subscribe('/topic/telemetry', function (message) {
        const telemetry = JSON.parse(message.body);
        updateTruckTable(telemetry);
        updateChart(telemetry);
    });

    // Subscribe to Alerts
    stompClient.subscribe('/topic/alerts', function (message) {
        const alert = JSON.parse(message.body);
        showAlert(alert);
    });
});

function updateTruckTable(data) {
    truckDataMap.set(data.truckId, data);
    renderTable();
    updateMapMarker(data);
}

function updateMapMarker(data) {
    if (truckMarkers.has(data.truckId)) {
        // Update existing marker
        const marker = truckMarkers.get(data.truckId);
        marker.setLatLng([data.latitude, data.longitude]);
        marker.setPopupContent(`<b>${data.truckId}</b><br>Speed: ${data.speed.toFixed(1)} mph`);
    } else {
        // Create new marker
        const marker = L.marker([data.latitude, data.longitude]).addTo(map);
        marker.bindPopup(`<b>${data.truckId}</b><br>Speed: ${data.speed.toFixed(1)} mph`);
        truckMarkers.set(data.truckId, marker);
    }
}

function renderTable() {
    truckTableBody.innerHTML = '';
    const sortedTrucks = Array.from(truckDataMap.values()).sort((a, b) => a.truckId.localeCompare(b.truckId));

    sortedTrucks.forEach(truck => {
        const row = document.createElement('tr');

        // Determine status class
        let statusClass = 'status-ok';
        let statusText = 'OK';
        if (truck.speed > 80) { statusClass = 'status-warn'; statusText = 'SPEEDING'; }
        if (truck.engineTemp > 100) { statusClass = 'status-crit'; statusText = 'OVERHEATING'; }
        if (truck.fuelLevel < 10) { statusClass = 'status-warn'; statusText = 'LOW FUEL'; }

        row.innerHTML = `
            <td>${truck.truckId}</td>
            <td>${truck.speed.toFixed(1)}</td>
            <td>${truck.engineTemp.toFixed(1)}</td>
            <td>
                <div style="width: 100px; background: #334155; height: 6px; border-radius: 3px; display: inline-block; margin-right: 5px;">
                    <div style="width: ${truck.fuelLevel}%; background: ${truck.fuelLevel < 20 ? '#ef4444' : '#22c55e'}; height: 100%; border-radius: 3px;"></div>
                </div>
                ${truck.fuelLevel.toFixed(0)}%
            </td>
            <td class="${statusClass}">${statusText}</td>
        `;
        truckTableBody.appendChild(row);
    });
}

function updateChart(data) {
    // Simple logic: Add data point for the current update (not perfect avg but shows activity)
    // In a real app, we'd aggregate this on the backend or maintain a rolling average here.

    const now = new Date().toLocaleTimeString();

    // Calculate average speed of all active trucks
    let totalSpeed = 0;
    truckDataMap.forEach(t => totalSpeed += t.speed);
    const avgSpeed = totalSpeed / truckDataMap.size;

    // Update numeric display
    const avgSpeedDisplay = document.getElementById('avg-speed-display');
    if (avgSpeedDisplay) {
        avgSpeedDisplay.innerText = avgSpeed.toFixed(1) + ' mph';
    }

    if (speedChart.data.labels.length > 20) {
        speedChart.data.labels.shift();
        speedChart.data.datasets[0].data.shift();
    }

    speedChart.data.labels.push(now);
    speedChart.data.datasets[0].data.push(avgSpeed);
    speedChart.update('none'); // 'none' mode for performance
}

function showAlert(alert) {
    const li = document.createElement('li');
    li.className = `alert-item ${alert.type}`;
    li.innerHTML = `
        <strong>${alert.type}</strong><br>
        ${alert.message}
        <div style="font-size: 0.7rem; opacity: 0.7; margin-top: 4px;">${new Date(alert.timestamp).toLocaleTimeString()}</div>
    `;
    alertsList.prepend(li);

    // Keep list size manageable
    if (alertsList.children.length > 50) {
        alertsList.removeChild(alertsList.lastChild);
    }
}
