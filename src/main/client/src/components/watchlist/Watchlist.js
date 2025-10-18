import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faHeart, faTrash, faEye } from '@fortawesome/free-solid-svg-icons';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { watchlistApi } from '../../api/watchlistApi';
import './Watchlist.css';

const Watchlist = () => {
  const { t } = useTranslation();
  const [watchlist, setWatchlist] = useState([]);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Kullanıcı kontrolü
    const userData = localStorage.getItem('user');
    if (!userData || !JSON.parse(userData).isLoggedIn) {
      navigate('/login');
      return;
    }
    
    setUser(JSON.parse(userData));
    loadWatchlist();
  }, [navigate]);

  const loadWatchlist = async () => {
    try {
      const userData = localStorage.getItem('user');
      if (userData) {
        const parsedUser = JSON.parse(userData);
        if (parsedUser.userId) {
          // Backend'den watchlist yükle
          const response = await watchlistApi.getUserWatchlist(parsedUser.userId);
          setWatchlist(response || []);
        } else {
          // Fallback: localStorage'dan yükle
          const watchlistKey = `watchlist_${parsedUser.email}`;
          const savedWatchlist = JSON.parse(localStorage.getItem(watchlistKey) || '[]');
          setWatchlist(savedWatchlist);
        }
      }
    } catch (error) {
      console.error('Watchlist yüklenirken hata:', error);
      // Fallback: localStorage'dan yükle
      const userData = localStorage.getItem('user');
      if (userData) {
        const parsedUser = JSON.parse(userData);
        const watchlistKey = `watchlist_${parsedUser.email}`;
        const savedWatchlist = JSON.parse(localStorage.getItem(watchlistKey) || '[]');
        setWatchlist(savedWatchlist);
      }
    } finally {
      setLoading(false);
    }
  };

  const removeFromWatchlist = async (movieId) => {
    try {
      if (user?.userId) {
        const response = await watchlistApi.removeFromWatchlist(user.userId, movieId);
        if (response.success) {
          setWatchlist(prev => prev.filter(movie => 
            (movie.imdbId !== movieId) && (movie.id !== movieId)
          ));
        } else {
          alert('Film listeden çıkarılamadı.');
        }
      } else {
        // Fallback: localStorage
        const updatedWatchlist = watchlist.filter(movie => movie.id !== movieId);
        setWatchlist(updatedWatchlist);
        const watchlistKey = `watchlist_${user.email}`;
        localStorage.setItem(watchlistKey, JSON.stringify(updatedWatchlist));
      }
    } catch (error) {
      console.error('Remove from watchlist error:', error);
      // Fallback: localStorage
      const updatedWatchlist = watchlist.filter(movie => 
        (movie.imdbId !== movieId) && (movie.id !== movieId)
      );
      setWatchlist(updatedWatchlist);
      const watchlistKey = `watchlist_${user.email}`;
      localStorage.setItem(watchlistKey, JSON.stringify(updatedWatchlist));
    }
  };

  const goToMovieDetails = (movieId) => {
    navigate(`/Reviews/${movieId}`);
  };

  if (loading) {
    return (
      <div className="watchlist-container">
        <Container>
          <div className="text-center py-5">
            <Spinner animation="border" role="status">
              <span className="visually-hidden">{t('common.loading')}</span>
            </Spinner>
            <p className="mt-3">{t('watchlist.loading')}</p>
          </div>
        </Container>
      </div>
    );
  }

  return (
    <div className="watchlist-container">
      <Container>
        <div className="watchlist-header">
          <h1 className="watchlist-title">
            <FontAwesomeIcon icon={faHeart} className="me-2" />
{t('watchlist.title')}
          </h1>
          <p className="watchlist-subtitle">
{t('watchlist.subtitle')} {user?.email}
          </p>
        </div>

        {watchlist.length === 0 ? (
          <div className="text-center py-5">
            <FontAwesomeIcon icon={faHeart} size="3x" className="text-muted mb-3" />
            <h3 className="text-muted">{t('watchlist.emptyList')}</h3>
            <p className="text-muted">{t('watchlist.noMoviesAdded')}</p>
            <Button variant="primary" onClick={() => navigate('/')}>
              {t('watchlist.discoverMovies')}
            </Button>
          </div>
        ) : (
          <>
            <Row>
              {watchlist.map((movie) => (
                <Col key={movie.id} xs={12} sm={6} md={4} lg={3} className="mb-4">
                  <Card className="watchlist-movie-card">
                    <div className="movie-poster-container">
                      <Card.Img
                        variant="top"
                        src={movie.poster}
                        alt={movie.title}
                        className="movie-poster"
                      />
                      <div className="movie-overlay">
                        <Button
                          variant="primary"
                          size="sm"
                          className="overlay-button"
                          onClick={() => goToMovieDetails(movie.imdbId)}
                        >
                          <FontAwesomeIcon icon={faEye} className="me-1" />
                          Detaylar
                        </Button>
                        <Button
                          variant="danger"
                          size="sm"
                          className="overlay-button"
                          onClick={() => removeFromWatchlist(movie.imdbId || movie.id)}
                        >
                          <FontAwesomeIcon icon={faTrash} />
                        </Button>
                      </div>
                    </div>
                    <Card.Body className="p-2">
                      <div className="d-flex justify-content-between align-items-start mb-2">
                        <Card.Title className="movie-title mb-0 flex-grow-1">
                          {movie.title}
                        </Card.Title>
                        <Button
                          variant="outline-danger"
                          size="sm"
                          className="remove-button"
                          onClick={(e) => {
                            e.stopPropagation();
                            removeFromWatchlist(movie.imdbId || movie.id);
                          }}
                        >
                          <FontAwesomeIcon icon={faTrash} />
                        </Button>
                      </div>
                      <div className="movie-info">
                        <div className="movie-genres">
                          {movie.genres?.slice(0, 2).map((genre, index) => (
                            <span key={index} className="genre-badge">
                              {genre}
                            </span>
                          ))}
                        </div>
                        {movie.imdbRating && (
                          <div className="movie-rating">
                            <span className="star-icon">⭐</span>
                            <span className="rating-value">{movie.imdbRating}</span>
                          </div>
                        )}
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          </>
        )}
      </Container>
    </div>
  );
};

export default Watchlist;
