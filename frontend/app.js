const API_BASE_URL = 'http://localhost:8080/api/rules'; // Adjust this URL if your backend runs on a different port

// Function to create a rule and display the AST
function createRule() {
    const ruleString = document.getElementById('rule-string').value;

    fetch(`${API_BASE_URL}/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ruleString }),
    })
    .then(response => response.json())
    .then(data => {
        // Display the AST
        document.getElementById('ast-output').innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
    })
    .catch(error => console.error('Error:', error));
}

// Function to evaluate the rule against user data
function evaluateRule() {
    const ruleString = document.getElementById('rule-string').value;
    const userData = document.getElementById('user-data').value;

    // Convert the JSON string into a valid object
    let data;
    try {
        data = JSON.parse(userData);
    } catch (e) {
        alert('Invalid JSON format in user data');
        return;
    }

    fetch(`${API_BASE_URL}/evaluate`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            rule: ruleString,
            data: data,
        }),
    })
    .then(response => response.json())
    .then(result => {
        // Display the result
        const evalResult = result.result ? 'True' : 'False';
        document.getElementById('evaluation-result').innerHTML = `<strong>${evalResult}</strong>`;
    })
    .catch(error => console.error('Error:', error));
}

// Function to combine two rules with a selected operator
function combineRules() {
    const rule1 = document.getElementById('rule1').value;
    const rule2 = document.getElementById('rule2').value;
    const operator = document.getElementById('operator').value;

    fetch(`${API_BASE_URL}/combine`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ rule1, rule2, operator }),
    })
    .then(response => response.json())
    .then(data => {
        // Display the combined AST
        document.getElementById('combined-ast-output').innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
    })
    .catch(error => console.error('Error:', error));
}
