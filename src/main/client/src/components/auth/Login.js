import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEnvelope, faLock, faSignInAlt } from '@fortawesome/free-solid-svg-icons';
import { authApi } from '../../api/authApi';
import './Auth.css';

const Login = ({ updateUser }) => {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

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

  return (
    <div className="auth-container">
      <Container>
        <Row className="justify-content-center">
          <Col md={6} lg={4}>
            <Card className="auth-card">
              <Card.Body>
                <div className="auth-header">
                  <h2 className="auth-title">Giriş Yap</h2>
                  <p className="auth-subtitle">Hesabınıza giriş yapın</p>
                </div>

                {error && <Alert variant="danger">{error}</Alert>}

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-4">
                    <Form.Label>
                      <FontAwesomeIcon icon={faEnvelope} className="me-2" />
                      Email Adresiniz
                    </Form.Label>
                    <Form.Control
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      placeholder="ornek@email.com"
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label>
                      <FontAwesomeIcon icon={faLock} className="me-2" />
                      Şifreniz
                    </Form.Label>
                    <Form.Control
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Şifrenizi girin"
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
                    {loading ? 'Giriş yapılıyor...' : 'Giriş Yap'}
                  </Button>
                </Form>

                <div className="auth-footer">
                  <p className="text-center mt-3">
                    Hesabınız yok mu? <Link to="/register">Kayıt olun</Link>
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
