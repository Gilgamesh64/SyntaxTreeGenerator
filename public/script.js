
const NODE_RADIUS = 24;
const HORIZONTAL_SPACING = 140;
const VERTICAL_SPACING = 120;
const MARGIN = 50;

let currentZoom = null;
let currentSvg = null;
let currentUpdateLinks = null;
let showEmpty = false;


async function loadTree(message) {

    const status = document.getElementById("status");

    status.textContent = "Loading tree...";

    try {

        const response = await fetch("/message", {
            method: "POST",
            headers: {
                "Content-Type": "text/plain"
            },
            body: message.toLowerCase()
        });

        const json = await response.json();

        if (!response.ok) {
            status.textContent =
                `HTTP status: ${response.status}\n` +
                `Error code: ${json.errorCode}\n` +
                `Message: ${json.message}`;
            return;
        }

        console.log(json);

        renderTree(json);

        status.textContent = "";

    } catch (error) {

        console.error("Error loading tree:", error);

        status.textContent =
            "Unable to load tree: " + error.message;
    }
}


/*
 * Convert binary tree JSON to D3 hierarchy data.
 *
 * Placeholders preserve left/right positioning
 * when a node has only one child.
 */
function prepareTree(node) {

    if (!node) return null;

    const result = {
        value: node.value,
        word: node.word,
        connection: node.connection,
        placeholder: false
    };

    const hasLeft = node.left != null;
    const hasRight = node.right != null;

    if (hasLeft || hasRight) {

        result.children = [

            hasLeft
                ? prepareTree(node.left)
                : { placeholder: true },

            hasRight
                ? prepareTree(node.right)
                : { placeholder: true }

        ];
    }

    return result;
}


/*
 * Render the binary tree
 */
function renderTree(data) {

    const container =
        document.getElementById("tree-container");

    container.innerHTML = "";

    currentZoom = null;
    currentSvg = null;
    currentUpdateLinks = null;

    if (!data) {
        document.getElementById("status")
            .textContent = "The tree is empty.";
        return;
    }

    const preparedData = prepareTree(data);

    const root = d3.hierarchy(
        preparedData,
        d => d.children
    );


    // Calculate tree positions
    const layout = d3.tree()
        .nodeSize([
            HORIZONTAL_SPACING,
            VERTICAL_SPACING
        ]);

    layout(root);

    const allNodes = root.descendants();

    const visibleNodes = allNodes.filter(
        d => !d.data.placeholder
    );

    // Create SVG
    const svg = d3.select("#tree-container")
        .append("svg")
        .attr("width", "100%")
        .attr("height", "100%")
        .attr(
            "viewBox",
            `0 0 ${window.innerWidth} ${window.innerHeight}`
        );

    currentSvg = svg;

    svg.classed(
        "minimal-view",
        button.classList.contains("minimal")
    );


    const drawing = svg.append("g");

    /*
     * Find pairs of nodes sharing a connection ID
     */
    const connectionGroups = new Map();

    visibleNodes.forEach(node => {

        const rawConnection = node.data.connection;

        if (rawConnection == null || rawConnection === "") {
            return;
        }

        // Accept both integer and string IDs
        const connection = String(rawConnection);

        if (!connectionGroups.has(connection)) {
            connectionGroups.set(connection, []);
        }

        connectionGroups.get(connection).push(node);
    });


    const connections = [];

    connectionGroups.forEach((nodes, id) => {

        if (nodes.length === 2) {

            connections.push({
                id,
                source: nodes[0],
                target: nodes[1]
            });

        } else {

            console.warn(
                `Connection ${id} has ${nodes.length} nodes instead of 2`,
                nodes
            );

        }

    });

    /*
     * Define arrowhead marker
     */
    const defs = svg.append("defs");

    defs.append("marker")
        .attr("id", "connection-arrow")
        .attr("viewBox", "0 0 10 10")
        .attr("refX", 8)
        .attr("refY", 5)
        .attr("markerWidth", 4)
        .attr("markerHeight", 4)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M 0 0 L 10 5 L 0 10 z")
        .attr("fill", "#94a3b8");


    /*
     * Draw connection arcs
     */
    const connectionGroup = drawing.append("g")
        .attr("class", "connections");


    connectionGroup.selectAll(".connection-link")
        .data(connections)
        .join("path")
        .attr("class", "connection-link")
        .attr("fill", "none")
        .attr("stroke", "#94a3b8")
        .attr("stroke-width", 1.5)
        .attr("marker-end", "url(#connection-arrow)")
        .attr("d", d => {

            // Direction: second node -> first node

            const gap = NODE_RADIUS;

            // Start at the bottom of the second node
            const startX = d.target.x;
            const startY = d.target.y + gap;

            // End at the bottom of the first node
            const endX = d.source.x;
            const endY = d.source.y + gap;

            // Midpoint between the two nodes
            const midpointX = (startX + endX) / 2;
            const midpointY = (startY + endY) / 2;

            const distance = Math.hypot(endX - startX, endY - startY);
            const bend = distance * 0.5;

            // Push the curve downward
            const controlX = midpointX;
            const controlY = midpointY + bend;

            return `
        M ${startX},${startY}
        Q ${controlX},${controlY}
          ${endX},${endY}`;
        });

    const links = root.links();

    const linkGenerator = d3.linkVertical()
        .x(d => d.x)
        .y(d => d.y);

    const linkElements = drawing.selectAll(".link")
        .data(links)
        .join("path")
        .attr("class", d =>
            d.target.data.placeholder
                ? "link empty-link"
                : "link"
        );


    function updateLinks() {

        const isMinimal =
            button.classList.contains("minimal");

        linkElements
            .style("display", d =>
                d.target.data.placeholder && !showEmpty
                    ? "none"
                    : null
            )
            .attr("d", d => {

                if (isMinimal) {

                    const dx = d.target.x - d.source.x;
                    const dy = d.target.y - d.source.y;

                    const distance = Math.sqrt(
                        dx * dx + dy * dy
                    );

                    const startGap = 30;
                    const endGap = 30;

                    const unitX = dx / distance;
                    const unitY = dy / distance;

                    const startX =
                        d.source.x + unitX * startGap;

                    const startY =
                        d.source.y + unitY * startGap;

                    const endX =
                        d.target.x - unitX * endGap;

                    const endY =
                        d.target.y - unitY * endGap;

                    return `M${startX},${startY} L${endX},${endY}`;
                }

                return linkGenerator(d);
            });
    }

    currentUpdateLinks = updateLinks;

    updateLinks();



    /*
     * Draw nodes
     */
    const nodes = drawing.selectAll(".node")
        .data(visibleNodes)
        .join("g")
        .attr("class", "node")
        .attr(
            "transform",
            d => `translate(${d.x}, ${d.y})`
        );


    // Node circles
    nodes.append("circle")
        .attr("r", NODE_RADIUS);


    // Node values
    nodes.append("text")
        .text(d => String(d.data.value ?? ""));


    // Display words below leaf nodes
    nodes.filter(d => d.data.word)
        .append("text")
        .attr("class", "modifier")
        .attr("y", NODE_RADIUS + 20)
        .text(d => d.data.word);


    // Calculate initial position
    const minX = d3.min(allNodes, d => d.x);

    const offsetX = MARGIN - minX;
    const offsetY = MARGIN;


    /*
     * Zoom behavior
     */
    const zoom = d3.zoom()
        .scaleExtent([0.2, 3])
        .on("zoom", event => {
            drawing.attr(
                "transform",
                event.transform
            );
        });

    currentZoom = zoom;

    svg.call(zoom);

    svg.call(
        zoom.transform,
        d3.zoomIdentity.translate(
            offsetX,
            offsetY
        )
    );
}


/*
 * Zoom controls
 */
function changeZoom(factor) {

    if (!currentSvg || !currentZoom) return;

    currentSvg.transition()
        .duration(200)
        .call(
            currentZoom.scaleBy,
            factor
        );
}


/*
 * Reset zoom and position
 */
function resetZoom() {

    if (!currentSvg || !currentZoom) return;

    currentSvg.transition()
        .duration(300)
        .call(
            currentZoom.transform,
            d3.zoomIdentity
        );
}



document.getElementById("zoom-in")
    .addEventListener("click", () => changeZoom(1.3));

document.getElementById("showEmpty")
    .addEventListener("click", () => {

        showEmpty = !showEmpty;

        if (currentUpdateLinks) {
            currentUpdateLinks();
        }
    });

document.getElementById("zoom-out")
    .addEventListener("click", () => changeZoom(0.7));

const button = document.getElementById("styleToggle");

button.addEventListener("click", () => {

    const isPretty = button.classList.contains("pretty");

    button.classList.toggle("pretty", !isPretty);
    button.classList.toggle("minimal", isPretty);

    button.textContent = isPretty ? "Minimal" : "Pretty";

    if (currentSvg) {
        currentSvg.classed("minimal-view", isPretty);
    }

    if (currentUpdateLinks) {
        currentUpdateLinks();
    }
});