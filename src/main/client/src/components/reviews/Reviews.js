import {useEffect, useRef, useState} from 'react';
import api from '../../api/axiosConfig';
import {useParams} from 'react-router-dom';
import {Container, Row, Col, Button, Badge} from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import ReviewForm from '../reviewForm/ReviewForm';
import './Reviews.css';

import React from 'react'

const Reviews = ({getMovieData,movie,reviews,setReviews,user}) => {
    const { t } = useTranslation();
    const revText = useRef();
    const [showDetailedForm, setShowDetailedForm] = useState(false);
    let params = useParams();
    const movieId = params.movieId;

    useEffect(()=>{
        getMovieData(movieId);
    },[])

    // Reviews yüklendikten sonra userName field'larını kontrol et
    useEffect(() => {
        if (reviews && reviews.length > 0 && user) {
            const needsUpdate = reviews.some(review => 
                !review.userName && review.userId === user.userId
            );
            
            if (needsUpdate) {
                const updatedReviews = reviews.map(review => {
                    // Eğer review'da userName yoksa ama current user'ın review'i ise, ekle
                    if (!review.userName && review.userId === user.userId) {
                        return {
                            ...review,
                            userName: user.name,
                            userEmail: user.email
                        };
                    }
                    return review;
                });
                setReviews(updatedReviews);
            }
        }
    }, []) // Sadece component mount olduğunda çalışsın

    const addReview = async (e) =>{
        e.preventDefault();

        const rev = revText.current;

        if (!user) {
            alert(t('reviews.loginError'));
            return;
        }

        if (!rev.value.trim()) {
            alert(t('reviews.enterReviewText'));
            return;
        }

        try
        {
            // Ensure UserId header is sent
            const config = {
                headers: {
                    'UserId': user.userId
                }
            };
            
            const response = await api.post("/reviews", {reviewBody: rev.value, imdbId: movieId}, config);

            // Backend'den güncellenmiş movie data'sını kullan
            if (response.data && response.data.length > 0) {
                const updatedMovie = response.data[0];
                setReviews(updatedMovie.reviewIds || []);
            }
            
            // Ayrıca sayfa verilerini de yenile
            getMovieData(movieId);
            rev.value = "";
        }
        catch(err)
        {
            console.error(err);
            alert(t('reviews.reviewError'));
        }
    }

    const addDetailedReview = async (reviewData) => {
        if (!user) {
            alert(t('reviews.loginError'));
            return;
        }

        if (!reviewData.reviewBody.trim()) {
            alert(t('reviews.enterReviewText'));
            return;
        }

        try {
            const config = {
                headers: {
                    'UserId': user.userId
                }
            };

            const payload = {
                ...reviewData,
                imdbId: movieId
            };
            
            const response = await api.post("/reviews/detailed", payload, config);

            if (response.data && response.data.length > 0) {
                const updatedMovie = response.data[0];
                setReviews(updatedMovie.reviewIds || []);
            }
            
            getMovieData(movieId);
            setShowDetailedForm(false);
            
            // Form'u temizle
            if (revText.current) {
                revText.current.value = "";
            }
        } catch (err) {
            console.error(err);
            alert(t('reviews.detailedReviewError'));
        }
    };

    const voteReview = async (reviewId, isHelpful) => {
        try {
            const config = {
                headers: {
                    'UserId': user?.userId || null
                }
            };
            
            await api.post(`/reviews/${reviewId}/vote`, { isHelpful }, config);
            getMovieData(movieId); // Refresh data
        } catch (err) {
            console.error('Error voting on review:', err);
        }
    };

  return (
    <div className="reviews-container">
      <Container>
        {/* Movie Header */}
        <div className="movie-header mb-5">
          <Row className="align-items-center">
            <Col md={3} className="text-center">
              <div className="movie-poster-container">
                <img src={movie?.poster} alt={movie?.title} className="movie-poster" />
              </div>
            </Col>
            <Col md={9}>
              <div className="movie-info">
                <h1 className="movie-title">{movie?.title}</h1>
                
                {/* Movie Meta Information */}
                <div className="movie-meta-info">
                  {movie?.imdbRating && (
                    <div className="movie-rating">
                      <span className="rating-label">IMDB</span>
                      <span className="rating-value">{movie.imdbRating}</span>
                    </div>
                  )}
                  {movie?.releaseDate && (
                    <div className="release-year">
                      {new Date(movie.releaseDate).getFullYear()}
                    </div>
                  )}
                  {movie?.duration && (
                    <div className="duration">
                      {Math.floor(movie.duration / 60)}h {movie.duration % 60}m
                    </div>
                  )}
                  {movie?.ageRating && (
                    <div className="age-rating">
                      {movie.ageRating}
                    </div>
                  )}
                </div>

                {/* Genres */}
                {movie?.genres && (
                  <div className="movie-genres mb-3">
                    {movie.genres.slice(0, 5).map((genre, idx) => (
                      <span key={idx} className="genre-tag">{genre}</span>
                    ))}
                  </div>
                )}

                {/* Movie Description */}
                {movie?.description && (
                  <div className="movie-description mb-3">
                    <p className="description-text">{movie.description}</p>
                  </div>
                )}

                {/* Director & Cast Information */}
                <div className="movie-crew-info">
                  {movie?.director && (
                    <div className="crew-item">
                      <span className="crew-label">Yönetmen:</span>
                      <span className="crew-value">{movie.director}</span>
                    </div>
                  )}
                  {movie?.cast && (
                    <div className="crew-item">
                      <span className="crew-label">Oyuncular:</span>
                      <span className="crew-value">
                        {movie.cast.length > 150 ? movie.cast.substring(0, 150) + '...' : movie.cast}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </Col>
          </Row>
        </div>

        {/* Reviews Section */}
        <div className="reviews-section">
          <div className="section-header">
            <h2 className="section-title">
              <span className="title-icon">💬</span>
{t('reviews.reviews')} ({reviews?.length || 0})
            </h2>
          </div>

          {/* Review Form */}
          <div className="review-form-section">
            {user ? (
              <div className="review-form-card">
                <div className="form-header">
                  <h4>{t('reviews.writeReview')}</h4>
                  <p>{t('reviews.shareThoughts')}</p>
                  <div className="form-type-buttons">
                    <Button 
                      variant={!showDetailedForm ? "primary" : "outline-primary"}
                      size="sm"
                      onClick={() => setShowDetailedForm(false)}
                      className="form-type-btn"
                    >
                      Hızlı Yorum
                    </Button>
                    <Button 
                      variant={showDetailedForm ? "primary" : "outline-primary"}
                      size="sm"
                      onClick={() => setShowDetailedForm(true)}
                      className="form-type-btn"
                    >
                      Detaylı Yorum
                    </Button>
                  </div>
                </div>
                <ReviewForm 
                  handleSubmit={addReview} 
                  revText={revText} 
                  labelText="Ne düşündünüz?" 
                  isDetailed={showDetailedForm}
                  onDetailedSubmit={addDetailedReview}
                />
              </div>
            ) : (
              <div className="login-prompt">
                <div className="prompt-icon">🔒</div>
                <h4>Yorum yazmak için giriş yapın</h4>
                <p>Sohbete katılın ve bu film hakkındaki düşüncelerinizi paylaşın.</p>
                <a href="/login" className="login-btn">Giriş Yap</a>
              </div>
            )}
          </div>

          {/* Reviews List */}
          <div className="reviews-list">
            {reviews && reviews.length > 0 ? (
              reviews.map((r, index) => {
                const formatDate = (dateString) => {
                  if (!dateString) return '';
                  const date = new Date(dateString);
                  return date.toLocaleDateString('tr-TR', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  });
                };

                // Güvenli userName kontrolü
                const displayName = r.userName || r.name || 'Anonim';
                const avatarLetter = displayName === 'Anonim' ? 'A' : displayName.charAt(0).toUpperCase();

                // Review detayları
                const renderRating = () => {
                  if (r.rating) {
                    return (
                      <div className="review-rating">
                        <span className="rating-label">Puan:</span>
                        <div className="stars-display">
                          {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((star) => (
                            <span key={star} className={`star ${star <= r.rating ? 'filled' : ''}`}>⭐</span>
                          ))}
                          <span className="rating-value">{r.rating}/10</span>
                        </div>
                      </div>
                    );
                  }
                  return null;
                };

                const renderTags = () => {
                  if (r.tags && r.tags.length > 0) {
                    return (
                      <div className="review-tags">
                        {r.tags.map((tag, tagIndex) => (
                          <Badge key={tagIndex} variant="secondary" className="review-tag">
                            {tag}
                          </Badge>
                        ))}
                      </div>
                    );
                  }
                  return null;
                };

                const renderVoting = () => {
                  if (r.helpfulVotes !== undefined || r.totalVotes !== undefined) {
                    const helpfulCount = r.helpfulVotes || 0;
                    const totalCount = r.totalVotes || 0;
                    const helpfulPercentage = totalCount > 0 ? Math.round((helpfulCount / totalCount) * 100) : 0;

                    return (
                      <div className="review-voting">
                        <span className="helpful-text">
                          {t('reviews.helpfulText', { 
                            percentage: helpfulPercentage, 
                            helpfulCount: helpfulCount, 
                            totalCount: totalCount 
                          })}
                        </span>
                        <div className="voting-buttons">
                          <Button 
                            size="sm" 
                            variant="outline-success"
                            onClick={() => voteReview(r._id, true)}
                            className="vote-btn"
                          >
{t('reviews.helpful')}
                          </Button>
                          <Button 
                            size="sm" 
                            variant="outline-secondary"
                            onClick={() => voteReview(r._id, false)}
                            className="vote-btn"
                          >
{t('reviews.notHelpful')}
                          </Button>
                        </div>
                      </div>
                    );
                  }
                  return null;
                };

                return (
                  <div key={index} className="review-card">
                    <div className="review-header">
                      <div className="reviewer-info">
                        <div className="reviewer-avatar">
                          {avatarLetter}
                        </div>
                        <div className="reviewer-details">
                          <h5 className="reviewer-name">{displayName}</h5>
                          <div className="review-meta">
                            {r.createdAt && (
                              <span className="review-date">{formatDate(r.createdAt)}</span>
                            )}
                            {r.reviewType && (
                              <Badge 
                                variant={r.reviewType === 'spoiler' ? 'warning' : 'info'} 
                                className="review-type-badge"
                              >
                                {r.reviewType === 'spoiler' ? t('reviews.spoilerContains') : t('reviews.spoilerFree')}
                              </Badge>
                            )}
                            {r.isRecommended !== undefined && (
                              <Badge 
                                variant={r.isRecommended ? 'success' : 'danger'} 
                                className="recommendation-badge"
                              >
                                {r.isRecommended ? t('reviews.recommends') : t('reviews.notRecommend')}
                              </Badge>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    {r.title && (
                      <div className="review-title">
                        <h6>{r.title}</h6>
                      </div>
                    )}

                    {renderRating()}
                    
                    <div className="review-content">
                      <p>{r.body}</p>
                    </div>

                    {renderTags()}
                    {renderVoting()}
                  </div>
                );
              })
            ) : (
              <div className="no-reviews">
                <div className="no-reviews-icon">📝</div>
                <h4>Henüz yorum yok</h4>
                <p>Bu film hakkında ilk düşüncenizi paylaşın!</p>
              </div>
            )}
          </div>
        </div>
      </Container>
    </div>
  )
}

export default Reviews