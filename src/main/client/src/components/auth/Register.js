import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUser, faEnvelope, faLock, faUserPlus } from '@fortawesome/free-solid-svg-icons';
import { useTranslation } from 'react-i18next';
import { authApi } from '../../api/authApi';
import useGoogleAuth from '../../hooks/useGoogleAuth';
import './Auth.css';

const Register = ({ updateUser }) => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  
  // Google OAuth configuration - TEST MODE
  // Replace with your actual Google Client ID from Google Cloud Console
  const GOOGLE_CLIENT_ID = process.env.REACT_APP_GOOGLE_CLIENT_ID || 'your-google-client-id.apps.googleusercontent.com';
  const { isLoaded, initializeGoogleSignIn, renderGoogleSignInButton } = useGoogleAuth(GOOGLE_CLIENT_ID);

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

    // Şifre kontrolü
    if (formData.password !== formData.confirmPassword) {
      setError('Şifreler eşleşmiyor.');
      setLoading(false);
      return;
    }

    // Şifre uzunluk kontrolü
    if (formData.password.length < 6) {
      setError('Şifre en az 6 karakter olmalıdır.');
      setLoading(false);
      return;
    }

    try {
      const { confirmPassword, ...registerData } = formData;
      const response = await authApi.register(registerData);
      
      if (response.success) {
        navigate('/login');
        alert('Kayıt başarılı! Şimdi giriş yapabilirsiniz.');
      } else {
        setError(response.message || 'Kayıt olunamadı. Lütfen tekrar deneyin.');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Kayıt olunamadı. Lütfen tekrar deneyin.');
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
      setError(err.response?.data?.message || 'Sunucu hatası. Lütfen tekrar deneyin.');
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
        renderGoogleSignInButton('google-signin-button-register');
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
                  <h2 className="auth-title">{t('auth.registerTitle')}</h2>
                  <p className="auth-subtitle">{t('auth.registerSubtitle')}</p>
                </div>

                {error && <Alert variant="danger">{error}</Alert>}

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-4">
                    <Form.Label>
                      <FontAwesomeIcon icon={faUser} className="me-2" />
{t('auth.name')}
                    </Form.Label>
                    <Form.Control
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleChange}
                      placeholder={t('auth.namePlaceholder')}
                      required
                    />
                  </Form.Group>

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
                      placeholder={t('auth.passwordMinLength')}
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label>
                      <FontAwesomeIcon icon={faLock} className="me-2" />
{t('auth.confirmPassword')}
                    </Form.Label>
                    <Form.Control
                      type="password"
                      name="confirmPassword"
                      value={formData.confirmPassword}
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
                    <FontAwesomeIcon icon={faUserPlus} className="me-2" />
                    {loading ? t('common.loading') : t('auth.registerTitle')}
                  </Button>
                </Form>

                <div className="auth-divider">
                  <hr />
                  <span className="divider-text">{t('auth.or')}</span>
                  <hr />
                </div>

                <div className="google-signin-container">
                  <div id="google-signin-button-register"></div>
                </div>

                <div className="auth-footer">
                  <p className="text-center mt-3">
{t('auth.hasAccount')} <Link to="/login">{t('navbar.login')}</Link>
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

export default Register;
