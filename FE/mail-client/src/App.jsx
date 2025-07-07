import { Routes, Route, Link } from 'react-router-dom';
import Inbox from './components/Inbox.jsx';
import SentBox from './components/SentBox.jsx';
import EmailDetail from './components/EmailDetail.jsx';
import ComposeMail from './components/ComposeMail.jsx';

function App() {
    return (
        <div className="container-fluid">
            <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
                <div className="container">
                    <a className="navbar-brand" href="#">Email Client</a>
                    <div className="navbar-nav">
                        <Link to="/" className="nav-link">Inbox</Link>
                        <Link to="/sent" className="nav-link">Sent Box</Link>
                        <Link to="/compose" className="nav-link">Compose</Link>
                    </div>
                </div>
            </nav>
            <Routes>
                <Route path="/" element={<Inbox />} />
                <Route path="/sent" element={<SentBox />} />
                <Route path="/email/:id" element={<EmailDetail />} />
                <Route path="/compose" element={<ComposeMail />} />
            </Routes>
        </div>
    );
}

export default App;