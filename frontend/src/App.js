import React from 'react';
import './App.css';
import ReactWordcloud from "react-wordcloud";
import {Tweet} from "react-twitter-widgets";
import signinImg from "./twitter-signin.png";

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
        tweets = props.content.ids.map(id => <Tweet tweetId={id}/>);
    }

    return (
        <div>
            <p>Topic: {props.content.topic}</p>
            <div id="tweet-view">
                {tweets}
            </div>
        </div>
    );
}

function LoginComponent(props) {
    switch (props.user) {
        case undefined:
            return null;

        case null:
            return (
                <React.Fragment>
                    Please sign in with twitter to use TopicCloud
                    <a href="/api/signin"><img src={signinImg} alt="Sign in with Twitter"/></a>
                </React.Fragment>
            );

        default:
            return <p id="loggedin-label">Logged in as: @{props.user}</p>;
    }
}

function App() {
    const [screenName, setScreenName] = React.useState("");
    const [wordCloud, setWordCloud] = React.useState(null);
    const [displayedTweets, setDisplayedTweets] = React.useState({});
    const [profileUrl, setProfileUrl] = React.useState(null);
    const [loggedInUser, setLoggedInUser] = React.useState(undefined);

    React.useEffect(() => {
        console.log("Checking logged in!");
        doPost("/api/loggedinuser", {}, data => setLoggedInUser(data.screenName ?? null));
    }, []);

    function onWordClick(word) {
        console.log("Clicked: " + word.text);
        setDisplayedTweets({topic: word.text, ids: word.tweetIds});
    }

    function getWordTooltip(word) {
        return `${word.text} - weight: ${word.value.toFixed(2)}, sentiment: ${word.score.toFixed(2)}`
    }

    function submit(event) {
        event.preventDefault();
        setDisplayedTweets({});
        doPost("api/keywords", {screenName: screenName, numKeywords: 100}, function (data) {
            let words = data.topics;
            console.log(words);
            setWordCloud(<ReactWordcloud words={words} size={[600, 400]}
                                         options={{scale: "log", rotations: 1, rotationAngles: [0], padding: 0}}
                                         callbacks={{
                                             getWordColor: getWordColor,
                                             onWordClick: onWordClick,
                                             getWordTooltip: getWordTooltip
                                         }}/>);
            doPost("api/twitterpfp", {screenName: screenName}, data => setProfileUrl(data.imageUrl));
        });
    }

    function screenNameInputChanged(event) {
        let value = event.target.value;
        if (value.length > 0 && !value.startsWith("@")) {
            value = "@" + value;
        }
        setScreenName(value);
    }

    return (
        <div className="App">
            <LoginComponent user={loggedInUser}/>
            {loggedInUser ?
                <React.Fragment>
                    <form onSubmit={submit}>
                        <input type="text" value={screenName} onChange={screenNameInputChanged}/>
                        <input type="submit" value="Generate"/>
                    </form>
                    <div id="info">
                        {profileUrl ? <img id="profile-img" src={profileUrl} alt="Profile"/> : null}
                        {wordCloud}
                    </div>
                    <TweetDisplay content={displayedTweets}/>
                </React.Fragment>
                : null
            }

        </div>
    );
}

export default App;
