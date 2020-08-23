import React from 'react';
import './App.css';
import ReactWordcloud from "react-wordcloud";

function doPost(endpoint, content, callback) {
    const http = new XMLHttpRequest();
    http.open("POST", endpoint);
    http.setRequestHeader("Content-Type", "application/json");
    http.onreadystatechange = function() {
        if (http.readyState === 4 && http.status === 200) {
            let contentType = http.getResponseHeader("Content-Type");
            if (contentType !== null && contentType.includes("application/json")) {
                console.log(http.responseText);
                callback(JSON.parse(http.responseText));
            }
        }
    }

    const body = JSON.stringify(content);
    http.send(body);
}

function App() {
    const [screenName, setScreenName] = React.useState("");
    const [wordCloud, setWordCloud] = React.useState(null);

    function submit(event) {
        event.preventDefault();
        doPost("api/keywords", {screenName: screenName, numKeywords: 100}, function(data) {
            let words = data.words;
            // words.forEach(w => w.value *= 10);
            console.log(words);
            setWordCloud(<ReactWordcloud words={words} size={[600,400]} options={{scale: "log", rotations: 1, rotationAngles: [0], padding: 0}}/>);
        });
    }

    return (
        <div className="App">
            <form onSubmit={submit}>
                <input type="text" value={screenName} onChange={event => setScreenName(event.target.value)}/>
                <input type="submit" value="Generate"/>
            </form>
            <center>{wordCloud}</center>

        </div>
    );
}

export default App;
