import { useEffect, useState } from "react";
import { Navigate, useLocation, useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";

function PostForm() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const { user, ready } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isEdit || !user) return;
    api(`/api/posts/${id}`)
      .then((post) => {
        if (post.authorId !== user.id) {
          setError("작성자 본인만 수정할 수 있습니다.");
          return;
        }
        setTitle(post.title);
        setContent(post.content);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id, isEdit, user]);

  if (!ready) return null;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) {
      setError("제목과 내용을 모두 입력해주세요.");
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      const post = await api(isEdit ? `/api/posts/${id}` : "/api/posts", {
        method: isEdit ? "PUT" : "POST",
        body: { title, content },
      });
      navigate(`/posts/${post.id}`, { replace: true });
    } catch (err) {
      setError(err.message);
      setSubmitting(false);
    }
  };

  if (loading) return <p className="empty">불러오는 중...</p>;

  return (
    <section>
      <h1 className="page-title">{isEdit ? "글 수정" : "글쓰기"}</h1>
      <form className="card form" onSubmit={handleSubmit}>
        <label className="field">
          <span>제목</span>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            maxLength={200}
            placeholder="제목을 입력하세요"
            autoFocus
          />
        </label>
        <label className="field">
          <span>내용</span>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={14}
            placeholder="내용을 입력하세요"
          />
        </label>
        {error && <p className="error">{error}</p>}
        <div className="actions">
          <button type="button" className="btn" onClick={() => navigate(-1)}>취소</button>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {isEdit ? "수정하기" : "등록하기"}
          </button>
        </div>
      </form>
    </section>
  );
}

export default PostForm;
