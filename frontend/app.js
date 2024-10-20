const API_BASE_URL = 'http://localhost:8080/api/rules'; 


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
        
        document.getElementById('ast-output').innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
    })
    .catch(error => console.error('Error:', error));
}


function evaluateRule() {
    const ruleString = document.getElementById('rule-string').value;
    const userData = document.getElementById('user-data').value;

    
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
        
        const evalResult = result.result ? 'True' : 'False';
        document.getElementById('evaluation-result').innerHTML = `<strong>${evalResult}</strong>`;
    })
    .catch(error => console.error('Error:', error));
}


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
        
        document.getElementById('combined-ast-output').innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
    })
    .catch(error => console.error('Error:', error));
}
function fetchRules() {
    fetch(`${API_BASE_URL}`) // Call the API endpoint
    .then(response => response.json())
    .then(data => {
        const rulesOutput = document.getElementById('rules-output');
        rulesOutput.innerHTML = ''; // Clear any previous content

        data.forEach(rule => {
            const li = document.createElement('li');
            li.textContent = rule.rule_string; // Assuming 'rule_string' contains the rule
            rulesOutput.appendChild(li);
        });
    })
    .catch(error => {
        console.error('Error fetching rules:', error);
    });
}

// Call fetchRules on page load to display all rules
window.onload = fetchRules;

