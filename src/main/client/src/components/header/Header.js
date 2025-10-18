import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faVideoSlash, faUser, faSignOutAlt, faHome, faHeart } from "@fortawesome/free-solid-svg-icons";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container"
import Nav from "react-bootstrap/Nav";
import Navbar from "react-bootstrap/Navbar";
import { NavLink, useNavigate } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import LanguageSwitcher from '../common/LanguageSwitcher';
import './Header.css';

const Header = ({ user, updateUser }) => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleLogout = () => {
    localStorage.removeItem('user');
    updateUser(null);
    navigate('/');
  };

  return (
    <Navbar className="custom-navbar" expand="lg" variant="dark">
      <Container fluid>
        <Navbar.Brand className="navbar-brand" href="/">
          <FontAwesomeIcon icon={faVideoSlash} />
          <span>Gold Cinema</span>
        </Navbar.Brand>
        
        <Navbar.Toggle aria-controls="navbarScroll" />
        
        <Navbar.Collapse id="navbarScroll">
          <Nav className="me-auto my-2 my-lg-0" navbarScroll>
            <NavLink className="custom-nav-link" to="/">
              <FontAwesomeIcon icon={faHome} className="me-2" />
              {t('navbar.home')}
            </NavLink>
            {user && (
              <NavLink className="custom-nav-link" to="/watchlist">
                <FontAwesomeIcon icon={faHeart} className="me-2" />
                {t('navbar.watchlist')}
              </NavLink>
            )}
          </Nav>
          
          <div className="d-flex align-items-center flex-wrap gap-2">
            <LanguageSwitcher />
            {user ? (
              <>
                <div className="user-info">
                  <FontAwesomeIcon icon={faUser} />
                  <span>{user.name || user.email}</span>
                </div>
                <Button className="logout-button" onClick={handleLogout}>
                  <FontAwesomeIcon icon={faSignOutAlt} className="me-2" />
                  {t('navbar.logout')}
                </Button>
              </>
            ) : (
              <>
                <NavLink to="/login">
                  <Button className="auth-button">
                    {t('navbar.login')}
                  </Button>
                </NavLink>
                <NavLink to="/register">
                  <Button className="auth-button">
                    {t('navbar.register')}
                  </Button>
                </NavLink>
              </>
            )}
          </div>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  )
}

export default Header