import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEnvelope, faLock, faSignInAlt } from '@fortawesome/free-solid-svg-icons';
import { useTranslation } from 'react-i18next';
import { authApi } from '../../api/authApi';
import useGoogleAuth from '../../hooks/useGoogleAuth';
import './Auth.css';

const Login = ({ updateUser }) => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  
  // Google OAuth configuration - TEST MODE
  // Replace with your actual Google Client ID from Google Cloud Console
  const GOOGLE_CLIENT_ID = process.env.REACT_APP_GOOGLE_CLIENT_ID || 'your-google-client-id.apps.googleusercontent.com';
  const { isLoaded, initializeGoogleSignIn, renderGoogleSignInButton } = useGoogleAuth(GOOGLE_CLIENT_ID);

  useEffect(() => {
    // Initialize Google Sign-In when component mounts
  }, [GOOGLE_CLIENT_ID]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await authApi.login(formData);
      
      if (response.success) {
        const userData = {
          email: response.email,
          name: response.name,
          userId: response.userId,
          token: response.token,
          isLoggedIn: true
        };
        localStorage.setItem('user', JSON.stringify(userData));
        updateUser(userData);
        navigate('/');
      } else {
        setError(response.message || 'Giriş yapılamadı. Email ve şifrenizi kontrol edin.');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Sunucu hatası. Lütfen tekrar deneyin.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignIn = useCallback(async (response) => {
    setLoading(true);
    setError('');

    try {
      const backendResponse = await authApi.loginWithGoogle(response.credential);
      
      if (backendResponse.success) {
        const userData = {
          email: backendResponse.email,
          name: backendResponse.name,
          userId: backendResponse.userId,
          token: backendResponse.token,
          isLoggedIn: true,
          provider: 'google'
        };
        localStorage.setItem('user', JSON.stringify(userData));
        updateUser(userData);
        navigate('/');
      } else {
        setError(backendResponse.message || 'Google ile giriş yapılamadı.');
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || 
                          err.message || 
                          'Sunucu hatası. Lütfen tekrar deneyin.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [updateUser, navigate]);

  useEffect(() => {
    if (isLoaded && initializeGoogleSignIn && handleGoogleSignIn) {
      initializeGoogleSignIn(handleGoogleSignIn);
    }
  }, [isLoaded, initializeGoogleSignIn, handleGoogleSignIn]);

  useEffect(() => {
    if (isLoaded && renderGoogleSignInButton) {
      // Small delay to ensure DOM element is ready
      const timer = setTimeout(() => {
        renderGoogleSignInButton('google-signin-button');
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [isLoaded, renderGoogleSignInButton]);

  return (
    <div className="auth-container">
      <Container>
        <Row className="justify-content-center">
          <Col md={6} lg={4}>
            <Card className="auth-card">
              <Card.Body>
                <div className="auth-header">
                  <h2 className="auth-title">{t('auth.loginTitle')}</h2>
                  <p className="auth-subtitle">{t('auth.loginSubtitle')}</p>
                </div>

                {error && <Alert variant="danger">{error}</Alert>}

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-4">
                    <Form.Label>
                      <FontAwesomeIcon icon={faEnvelope} className="me-2" />
{t('auth.email')}
                    </Form.Label>
                    <Form.Control
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      placeholder={t('auth.emailPlaceholder')}
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label>
                      <FontAwesomeIcon icon={faLock} className="me-2" />
{t('auth.password')}
                    </Form.Label>
                    <Form.Control
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder={t('auth.passwordPlaceholder')}
                      required
                    />
                  </Form.Group>

                  <Button 
                    variant="primary" 
                    type="submit" 
                    className="w-100 auth-button"
                    disabled={loading}
                  >
                    <FontAwesomeIcon icon={faSignInAlt} className="me-2" />
                    {loading ? t('common.loading') : t('auth.loginTitle')}
                  </Button>
                </Form>

                <div className="auth-divider">
                  <hr />
                  <span className="divider-text">{t('auth.or')}</span>
                  <hr />
                </div>

                <div className="google-signin-container">
                  <div id="google-signin-button"></div>
                </div>

                <div className="auth-footer">
                  <p className="text-center mt-3">
{t('auth.noAccount')} <Link to="/register">{t('navbar.register')}</Link>
                  </p>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default Login;
