import './Hero.css';
import Carousel from 'react-material-ui-carousel';
import { Paper } from '@mui/material';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCirclePlay, faVolumeMute, faVolumeUp, faHeart } from '@fortawesome/free-solid-svg-icons';
import { useNavigate} from "react-router-dom";
import Button from 'react-bootstrap/Button';
import { useState, useEffect, useRef, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import TrailerModal from '../trailer/TrailerModal';
import BackgroundTrailer from '../trailer/BackgroundTrailer';
import NotificationPopup from '../ui/NotificationPopup';
import { watchlistApi } from '../../api/watchlistApi';


const Hero = ({movies, user}) => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [selectedTrailer, setSelectedTrailer] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [backgroundTrailerId, setBackgroundTrailerId] = useState(null);
    const [activeSlideIndex, setActiveSlideIndex] = useState(0);
    const [isVideoPlaying, setIsVideoPlaying] = useState(true);
    const [isVideoMuted, setIsVideoMuted] = useState(true);
    const [showVideoControls, setShowVideoControls] = useState(false);
    const [isMouseOverVideo, setIsMouseOverVideo] = useState(false);
    const [movieWatchlistStatus, setMovieWatchlistStatus] = useState({});
    const [notification, setNotification] = useState({
        isVisible: false,
        message: '',
        type: 'success',
        action: 'added'
    });
    const backgroundTrailerRef = useRef(null);

    // Check watchlist status for movies
    const checkWatchlistStatus = useCallback(async () => {
        if (!user || !movies) return;
        
        const statusPromises = movies.map(async (movie) => {
            try {
                const response = await watchlistApi.checkInWatchlist(user.userId, movie.imdbId);
                // Boolean değeri garantilemek için - null/undefined kontrolü
                const isInWatchlist = Boolean(response?.inWatchlist);
                return { movieId: movie.imdbId, isInWatchlist };
            } catch (error) {
                console.error('Error checking watchlist status:', error);
                return { movieId: movie.imdbId, isInWatchlist: false };
            }
        });

        try {
            const results = await Promise.all(statusPromises);
            const statusMap = {};
            results.forEach(result => {
                statusMap[result.movieId] = result.isInWatchlist;
            });
            setMovieWatchlistStatus(statusMap);
        } catch (error) {
            console.error('Error updating watchlist status:', error);
        }
    }, [user, movies]);

    // Check watchlist status when user or movies change
    useEffect(() => {
        if (!user) {
            // Clear watchlist status when user logs out
            setMovieWatchlistStatus({});
        } else {
            checkWatchlistStatus();
        }
    }, [checkWatchlistStatus, user]);

    function reviews(movieId) {
        navigate(`/Reviews/${movieId}`);
    }

    const showNotification = (message, type = 'success', action = 'added') => {
        setNotification({
            isVisible: true,
            message,
            type,
            action
        });
    };

    const hideNotification = () => {
        setNotification(prev => ({ ...prev, isVisible: false }));
    };

    const toggleWatchlist = async (movie) => {
        if (!user) {
            navigate('/login');
            return;
        }

        const isInWatchlist = Boolean(movieWatchlistStatus[movie.imdbId]);

        try {
            if (user.userId && movie.imdbId) {
                // Backend API kullan
                let response;
                if (isInWatchlist) {
                    response = await watchlistApi.removeFromWatchlist(user.userId, movie.imdbId);
                } else {
                    response = await watchlistApi.addToWatchlist(user.userId, movie.imdbId);
                }
                
                if (response.success) {
                    // Update local state
                    setMovieWatchlistStatus(prev => ({
                        ...prev,
                        [movie.imdbId]: !isInWatchlist
                    }));
                    
                    const action = isInWatchlist ? 'removed' : 'added';
                    const message = isInWatchlist 
                        ? `${movie.title} ${t('watchlist.removedFromList')}` 
                        : `${movie.title} ${t('watchlist.addedToList')}`;
                    
                    showNotification(message, 'success', action);
                } else {
                    showNotification(response.message || 'İşlem sırasında hata oluştu.', 'error', 'error');
                }
            } else {
                // Fallback: localStorage
                const watchlistKey = `watchlist_${user.email}`;
                const existingWatchlist = JSON.parse(localStorage.getItem(watchlistKey) || '[]');
                
                const itemIndex = existingWatchlist.findIndex(item => item.imdbId === movie.imdbId);
                
                if (itemIndex >= 0) {
                    // Remove from watchlist
                    existingWatchlist.splice(itemIndex, 1);
                    setMovieWatchlistStatus(prev => ({
                        ...prev,
                        [movie.imdbId]: false
                    }));
                    showNotification(`${movie.title} izleme listenizden çıkarıldı!`, 'success', 'removed');
                } else {
                    // Add to watchlist
                    const newItem = {
                        id: Date.now(),
                        imdbId: movie.imdbId,
                        title: movie.title,
                        poster: movie.poster,
                        genres: movie.genres,
                        imdbRating: movie.imdbRating || 'N/A'
                    };
                    
                    existingWatchlist.push(newItem);
                    setMovieWatchlistStatus(prev => ({
                        ...prev,
                        [movie.imdbId]: true
                    }));
                    showNotification(`${movie.title} izleme listenize eklendi!`, 'success', 'added');
                }
                
                localStorage.setItem(watchlistKey, JSON.stringify(existingWatchlist));
            }
        } catch (error) {
            console.error('Watchlist toggle error:', error);
            showNotification('İşlem sırasında hata oluştu. Lütfen tekrar deneyin.', 'error', 'error');
        }
    };

    const openTrailerModal = (movie) => {
        const trailerId = movie.trailerLink?.substring(movie.trailerLink.length - 11);
        if (trailerId) {
            setSelectedTrailer({ id: trailerId, title: movie.title });
            setIsModalOpen(true);
        }
    };

    const closeTrailerModal = () => {
        setIsModalOpen(false);
        setSelectedTrailer(null);
    };

    // Video control functions
    const toggleVideoMute = () => {
        console.log('Mute button clicked');
        setIsVideoMuted(!isVideoMuted);
    };

    const toggleVideoPlayPause = () => {
        console.log('Video clicked - toggling play/pause, current state:', isVideoPlaying);
        setIsVideoPlaying(!isVideoPlaying);
        console.log('New state will be:', !isVideoPlaying);
    };


    const handleVideoMouseMove = () => {
        setIsMouseOverVideo(true);
    };

    const handleVideoMouseLeave = () => {
        setTimeout(() => setShowVideoControls(false), 3000);
        setIsMouseOverVideo(false);
    };


    const setBackgroundTrailer = (movie) => {
        const trailerId = movie.trailerLink?.substring(movie.trailerLink.length - 11);
        if (trailerId) {
            setBackgroundTrailerId(trailerId);
        } else {
            setBackgroundTrailerId(null);
        }
    };

    // Set background trailer when movies are loaded or active slide changes
    useEffect(() => {
        if (movies && movies.length > 0 && activeSlideIndex < movies.length) {
            setBackgroundTrailer(movies[activeSlideIndex]);
            // Reset video controls when slide changes
            setIsVideoPlaying(true);
            setIsVideoMuted(true);
            setShowVideoControls(false);
        }
    }, [movies, activeSlideIndex]);

    // Auto-advance every 30 seconds (only when mouse is not over video)
    useEffect(() => {
        if (movies && movies.length > 1 && !isMouseOverVideo) {
            const timer = setInterval(() => {
                setActiveSlideIndex((prevIndex) => (prevIndex + 1) % movies.length);
            }, 30000); // 30 seconds

            return () => clearInterval(timer);
        }
    }, [movies, isMouseOverVideo]);

  // Early return if no movies
  if (!movies || movies.length === 0) {
    return null;
  }

  return (
    <>
      <div className='movie-carousel-container'>
        <Carousel 
          index={activeSlideIndex}
          onChange={(index) => setActiveSlideIndex(index)}
          navButtonsAlwaysVisible={true}
          autoPlay={false}
          animation="slide"
          duration={800}
          cycleNavigation={true}
          indicators={false}
        >
          {
            movies?.map((movie, index) => {
              if (!movie) return null;
              return (
                <Paper key={movie.imdbId || index}>
                  <div className='movie-card-container'>
                    <div 
                      className={`movie-card ${index === activeSlideIndex && movie.trailerLink ? 'trailer-active' : ''}`}
                      style={index === activeSlideIndex && movie.trailerLink ? {} : {"--img": `url(${movie.backdrops?.[0] || movie.poster})`}}
                    >
                      {/* Background trailer - only show for active slide */}
                      {index === activeSlideIndex && movie.trailerLink && (
                        <div 
                          className="movie-card-background-trailer"
                          onMouseMove={handleVideoMouseMove}
                          onMouseLeave={handleVideoMouseLeave}
                        >
                          <BackgroundTrailer 
                            ref={backgroundTrailerRef}
                            trailerId={movie.trailerLink?.substring(movie.trailerLink.length - 11)} 
                            isPlaying={isVideoPlaying}
                            isMuted={isVideoMuted}
                            onVideoClick={toggleVideoPlayPause}
                          />
                        </div>
                      )}
                      
                      
                      {/* Netflix-style content overlay */}
                      <div 
                        className="hero-content"
                        onMouseMove={handleVideoMouseMove}
                        onMouseLeave={handleVideoMouseLeave}
                      >
                        <div className="hero-info">
                          {/* Movie Meta Info */}
                          <div className="movie-meta-info">
                            {movie.imdbRating && (
                              <div className="imdb-rating">
                                <span className="imdb-label">IMDB</span>
                                <span className="imdb-score">{movie.imdbRating}</span>
                              </div>
                            )}
                            {movie.releaseDate && (
                              <div className="release-year">
                                {new Date(movie.releaseDate).getFullYear()}
                              </div>
                            )}
                            {movie.ageRating && (
                              <div className="age-rating">
                                {movie.ageRating}
                              </div>
                            )}
                            {movie.duration && (
                              <div className="duration">
                                {Math.floor(movie.duration / 60)}h {movie.duration % 60}m
                              </div>
                            )}
                          </div>

                          {/* Genres */}
                          <div className="hero-genres">
                            {movie.genres && movie.genres.slice(0, 3).map((genre, idx) => (
                              <span key={idx} className="genre-tag">{genre}</span>
                            ))}
                          </div>

                          {/* Title */}
                          <h1 className="hero-title">{movie.title || t('hero.untitledMovie')}</h1>

                          {/* Description */}
                          <div className="hero-description">
                            <p>
                              {movie.description || t('hero.defaultDescription')}
                            </p>
                          </div>

                          {/* Additional Movie Info */}
                          <div className="movie-additional-info">
                            {movie.director && (
                              <div className="movie-info-item">
                                <span className="info-label">{t('reviews.director')}</span>
                                <span className="info-value">{movie.director}</span>
                              </div>
                            )}
                            {movie.cast && (
                              <div className="movie-info-item">
                                <span className="info-label">{t('reviews.cast')}</span>
                                <span className="info-value">
                                  {movie.cast.length > 100 ? movie.cast.substring(0, 100) + '...' : movie.cast}
                                </span>
                              </div>
                            )}
                          </div>
                          <div className="hero-buttons">
                            <button 
                              className="netflix-play-btn"
                              onClick={() => openTrailerModal(movie)}
                            >
                              <FontAwesomeIcon icon={faCirclePlay} className="play-icon" />
                              <span>{t('hero.play')}</span>
                            </button>
                            <button 
                              className="netflix-info-btn"
                              onClick={() => reviews(movie.imdbId)}
                            >
                              <span>ℹ</span>
                              <span>{t('hero.moreInfo')}</span>
                            </button>
                            {user && (() => {
                              const isInWatchlist = Boolean(movieWatchlistStatus[movie.imdbId]);
                              return (
                                <button 
                                  className={`netflix-watchlist-btn ${isInWatchlist ? 'in-watchlist' : ''}`}
                                  onClick={() => toggleWatchlist(movie)}
                                >
                                  <FontAwesomeIcon 
                                    icon={faHeart} 
                                    className={isInWatchlist ? 'filled-heart' : ''}
                                  />
                                  <span>
                                    {isInWatchlist ? t('hero.inList') : t('hero.addToList')}
                                  </span>
                                </button>
                              );
                            })()}
                            {/* Mute Button - Only show for active slide with trailer */}
                            {index === activeSlideIndex && movie.trailerLink && (
                              <button 
                                className="netflix-mute-btn"
                                onClick={(e) => {
                                  e.preventDefault();
                                  e.stopPropagation();
                                  console.log('Mute button clicked');
                                  toggleVideoMute();
                                }}
                              >
                                <FontAwesomeIcon icon={isVideoMuted ? faVolumeMute : faVolumeUp} />
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                      
                      {/* Keep poster for non-active slides */}
                      {index !== activeSlideIndex && (
                        <div className="movie-detail">
                          <div className="movie-poster">
                            <img src={movie.poster} alt={movie.title || 'Film'} />
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </Paper>
              )
            })
          }
        </Carousel>
      </div>


      {/* Trailer Modal */}
      <TrailerModal
        isOpen={isModalOpen}
        onClose={closeTrailerModal}
        trailerId={selectedTrailer?.id}
        title={selectedTrailer?.title || (movies && movies[activeSlideIndex] ? movies[activeSlideIndex].title : null) || t('hero.movieTrailer')}
      />

      {/* Notification Popup */}
      <NotificationPopup
        isVisible={notification.isVisible}
        message={notification.message}
        type={notification.type}
        action={notification.action}
        onClose={hideNotification}
        duration={3000}
      />
    </>
  )
}

export default Hero