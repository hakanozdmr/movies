import {Form, Button, Row, Col, FormCheck} from 'react-bootstrap';
import {useState, useRef} from 'react';
import { useTranslation } from 'react-i18next';
import './ReviewForm.css';

const ReviewForm = ({handleSubmit, revText, labelText, defaultValue, isDetailed = false, onDetailedSubmit}) => {
  const { t } = useTranslation();
  const [title, setTitle] = useState('');
  const [rating, setRating] = useState(5);
  const [reviewType, setReviewType] = useState('non-spoiler');
  const [tags, setTags] = useState([]);
  const [isRecommended, setIsRecommended] = useState(true);
  const [selectedTags, setSelectedTags] = useState([]);

  const titleRef = useRef();
  const tagsRef = useRef();

  const predefinedTags = t('reviewForm.predefinedTags', { returnObjects: true });

  const handleTagToggle = (tag) => {
    if (selectedTags.includes(tag)) {
      setSelectedTags(selectedTags.filter(t => t !== tag));
    } else {
      setSelectedTags([...selectedTags, tag]);
    }
  };

  const handleDetailedSubmit = (e) => {
    e.preventDefault();
    if (onDetailedSubmit) {
      onDetailedSubmit({
        title: titleRef.current?.value || '',
        reviewBody: revText.current?.value || '',
        rating: rating,
        reviewType: reviewType,
        tags: selectedTags,
        isRecommended: isRecommended
      });
    }
  };

  const renderStarRating = () => {
    return (
      <div className="star-rating">
        <Form.Label className="rating-label">Puanlama (1-10)</Form.Label>
        <div className="stars-container">
          {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((star) => (
            <button
              key={star}
              type="button"
              className={`star ${star <= rating ? 'filled' : ''}`}
              onClick={() => setRating(star)}
            >
              ⭐
            </button>
          ))}
          <span className="rating-value">{rating}/10</span>
        </div>
      </div>
    );
  };

  if (isDetailed) {
    return (
      <Form className="detailed-review-form">
        <Row>
          <Col md={12}>
            <Form.Group className="mb-3" controlId="reviewTitle">
              <Form.Label className="form-label">{t('reviewForm.reviewTitle')}</Form.Label>
              <Form.Control
                ref={titleRef}
                type="text"
                placeholder="Yorumunuz için çarpıcı bir başlık verin..."
                className="review-title-input"
              />
            </Form.Group>
          </Col>
        </Row>

        <Row>
          <Col md={6}>
            {renderStarRating()}
          </Col>
          <Col md={6}>
            <Form.Group className="mb-3" controlId="reviewType">
              <Form.Label className="form-label">Yorum Tipi</Form.Label>
              <Form.Select
                value={reviewType}
                onChange={(e) => setReviewType(e.target.value)}
                className="review-type-select"
              >
                <option value="non-spoiler">Spoiler İçermez</option>
                <option value="spoiler">Spoiler İçerir</option>
              </Form.Select>
            </Form.Group>
          </Col>
        </Row>

        <Form.Group className="mb-3" controlId="reviewTextarea">
          <Form.Label className="form-label">{labelText || "Yorumunuz"}</Form.Label>
          <Form.Control 
            ref={revText} 
            as="textarea" 
            rows={5} 
            defaultValue={defaultValue}
            className="review-textarea"
            placeholder="Bu film hakkındaki detaylı düşüncelerinizi paylaşın..."
          />
          <div className="form-char-count">
            <small>Diğer izleyicilere yardımcı olmak için yorumunuzda detaylı olun!</small>
          </div>
        </Form.Group>

        <Form.Group className="mb-3" controlId="reviewTags">
          <Form.Label className="form-label">Etiketler</Form.Label>
          <div className="tags-container">
            {predefinedTags.map((tag) => (
              <button
                key={tag}
                type="button"
                className={`tag-btn ${selectedTags.includes(tag) ? 'selected' : ''}`}
                onClick={() => handleTagToggle(tag)}
              >
                {tag}
              </button>
            ))}
          </div>
        </Form.Group>

        <Row>
          <Col md={6}>
            <Form.Group className="mb-3" controlId="isRecommended">
              <Form.Check
                type="checkbox"
                label="Bu filmi tavsiye ediyorum"
                checked={isRecommended}
                onChange={(e) => setIsRecommended(e.target.checked)}
                className="recommendation-check"
              />
            </Form.Group>
          </Col>
        </Row>

        <div className="form-actions">
          <Button 
            type="submit"
            onClick={handleDetailedSubmit} 
            className="submit-detailed-review-btn"
          >
            <span className="btn-icon">✨</span>
            Detaylı Yorum Gönder
          </Button>
        </div>
      </Form>
    );
  }

  // Basit form (mevcut)
  return (
    <Form className="modern-review-form">
        <Form.Group className="mb-4" controlId="reviewTextarea">
            <Form.Label className="form-label">{labelText}</Form.Label>
            <Form.Control 
                ref={revText} 
                as="textarea" 
                rows={4} 
                defaultValue={defaultValue}
                className="review-textarea"
                placeholder={t('reviewForm.simplePlaceholder')}
            />
            <div className="form-char-count">
                <small>{t('reviewForm.tellUsWhatYouThink')}</small>
            </div>
        </Form.Group>
        <div className="form-actions">
            <Button 
                type="submit"
                onClick={handleSubmit} 
                className="submit-review-btn"
            >
                <span className="btn-icon">🚀</span>
{t('reviewForm.submitReview')}
            </Button>
        </div>
    </Form>   
  )
}

export default ReviewForm