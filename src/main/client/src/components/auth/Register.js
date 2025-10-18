import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUser, faEnvelope, faLock, faUserPlus } from '@fortawesome/free-solid-svg-icons';
import { authApi } from '../../api/authApi';
import './Auth.css';

const Register = ({ updateUser }) => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
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

  return (
    <div className="auth-container">
      <Container>
        <Row className="justify-content-center">
          <Col md={6} lg={4}>
            <Card className="auth-card">
              <Card.Body>
                <div className="auth-header">
                  <h2 className="auth-title">Kayıt Ol</h2>
                  <p className="auth-subtitle">Yeni hesap oluşturun</p>
                </div>

                {error && <Alert variant="danger">{error}</Alert>}

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-4">
                    <Form.Label>
                      <FontAwesomeIcon icon={faUser} className="me-2" />
                      Ad Soyadınız
                    </Form.Label>
                    <Form.Control
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleChange}
                      placeholder="Adınız ve soyadınız"
                      required
                    />
                  </Form.Group>

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
                      placeholder="En az 6 karakter"
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label>
                      <FontAwesomeIcon icon={faLock} className="me-2" />
                      Şifre Tekrarı
                    </Form.Label>
                    <Form.Control
                      type="password"
                      name="confirmPassword"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      placeholder="Şifrenizi tekrar girin"
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
                    {loading ? 'Kayıt yapılıyor...' : 'Kayıt Ol'}
                  </Button>
                </Form>

                <div className="auth-footer">
                  <p className="text-center mt-3">
                    Zaten hesabınız var mı? <Link to="/login">Giriş yapın</Link>
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
