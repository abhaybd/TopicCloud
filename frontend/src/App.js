import React from 'react';
import './App.css';
import ReactWordcloud from "react-wordcloud";
import {Tweet} from "react-twitter-widgets";

function doPost(endpoint, content, callback) {
    const http = new XMLHttpRequest();
    http.open("POST", endpoint);
    http.setRequestHeader("Content-Type", "application/json");
    http.onreadystatechange = function () {
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

function getColor(score) {
    let r = 128, g = 128, b = 128;
    let targetR = 0, targetG = 0, targetB = 0;
    if (score > 0) {
        targetG = 255;
    } else {
        targetR = 255;
    }

    score = Math.cbrt(Math.abs(score));

    r += score * (targetR - r);
    g += score * (targetG - g);
    b += score * (targetB - b);

    return `rgb(${Math.round(r)},${Math.round(g)},${Math.round(b)})`;
}

function getWordColor(word) {
    return getColor(word.score);
}

function TweetDisplay(props) {
    if (!props.content.topic) return null;

    let tweets = null;
    if (props.content.ids) {
        tweets = props.content.ids.map(id => <Tweet tweetId={id} align="right"/>);
    }

    return (
        <div>
            <p>Topic: {props.content.topic}</p>
            <div style={{overflowY: "scroll", width: "700px", height: "400px", margin: "0 auto"}}>
                {tweets}
            </div>
        </div>
    );
}

function App() {
    const [screenName, setScreenName] = React.useState("");
    const [wordCloud, setWordCloud] = React.useState(null);
    const [displayedTweets, setDisplayedTweets] = React.useState({});

    function onWordClick(word) {
        console.log("Clicked: " + word.text);
        setDisplayedTweets({topic: word.text, ids: word.tweetIds});
    }

    function getWordTooltip(word) {
        return `${word.text} - weight: ${word.value.toFixed(2)}, sentiment: ${word.score.toFixed(2)}`
    }

    function submit(event) {
        event.preventDefault();
        doPost("api/keywords", {screenName: screenName, numKeywords: 200}, function (data) {
            let words = data.content;
            console.log(words);
            setWordCloud(<ReactWordcloud words={words} size={[600, 400]}
                                         options={{scale: "log", rotations: 1, rotationAngles: [0], padding: 0}}
                                         callbacks={{getWordColor: getWordColor, onWordClick: onWordClick, getWordTooltip: getWordTooltip}}/>);
        });
    }

    return (
        <div className="App">
            <form onSubmit={submit}>
                <input type="text" value={screenName} onChange={event => setScreenName(event.target.value)}/>
                <input type="submit" value="Generate"/>
            </form>
            <center>{wordCloud}</center>
            <TweetDisplay content={displayedTweets}/>
        </div>
    );
}

export default App;
