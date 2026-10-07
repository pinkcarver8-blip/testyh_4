import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api, formatDate } from "../api";
import { useAuth } from "../auth";

function PostDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [error, setError] = useState("");
  const [commentError, setCommentError] = useState("");

  useEffect(() => {
    Promise.all([api(`/api/posts/${id}`), api(`/api/posts/${id}/comments`)])
      .then(([p, c]) => {
        setPost(p);
        setComments(c);
      })
      .catch((e) => setError(e.message));
  }, [id]);

  const deletePost = async () => {
    if (!window.confirm("이 글을 삭제할까요?")) return;
    try {
      await api(`/api/posts/${id}`, { method: "DELETE" });
      navigate("/", { replace: true });
    } catch (e) {
      setError(e.message);
    }
  };

  const addComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    try {
      const created = await api(`/api/posts/${id}/comments`, {
        method: "POST",
        body: { content: newComment },
      });
      setComments((prev) => [...prev, created]);
      setNewComment("");
      setCommentError("");
    } catch (err) {
      setCommentError(err.message);
    }
  };

  const deleteComment = async (commentId) => {
    if (!window.confirm("댓글을 삭제할까요?")) return;
    try {
      await api(`/api/comments/${commentId}`, { method: "DELETE" });
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch (err) {
      setCommentError(err.message);
    }
  };

  if (error && !post) {
    return (
      <section>
        <p className="error">{error}</p>
        <Link to="/" className="btn">목록으로</Link>
      </section>
    );
  }
  if (!post) return <p className="empty">불러오는 중...</p>;

  const isMine = user?.id === post.authorId;

  return (
    <section>
      <article className="card post">
        <h1 className="post-title">{post.title}</h1>
        <div className="post-meta">
          <span>{post.author}</span>
          <span>{formatDate(post.createdAt, true)}</span>
          {post.updatedAt && <span>(수정됨 {formatDate(post.updatedAt, true)})</span>}
        </div>
        <div className="post-content">{post.content}</div>
        {error && <p className="error">{error}</p>}
      </article>

      <div className="actions">
        <Link to="/" className="btn">목록</Link>
        {isMine && (
          <>
            <Link to={`/posts/${id}/edit`} className="btn">수정</Link>
            <button className="btn btn-danger" onClick={deletePost}>삭제</button>
          </>
        )}
      </div>

      <section className="card comments">
        <h2 className="comments-title">댓글 {comments.length}</h2>
        <ul className="comment-list">
          {comments.map((c) => (
            <li key={c.id} className="comment">
              <div className="comment-head">
                <strong>{c.author}</strong>
                <span className="muted">{formatDate(c.createdAt, true)}</span>
                {user?.id === c.authorId && (
                  <button className="link-btn" onClick={() => deleteComment(c.id)}>삭제</button>
                )}
              </div>
              <p className="comment-body">{c.content}</p>
            </li>
          ))}
          {comments.length === 0 && <li className="muted">첫 댓글을 남겨보세요.</li>}
        </ul>

        {user ? (
          <form className="comment-form" onSubmit={addComment}>
            <textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              maxLength={1000}
              rows={3}
              placeholder="댓글을 입력하세요"
            />
            <button type="submit" className="btn btn-primary">등록</button>
          </form>
        ) : (
          <p className="muted">
            댓글을 쓰려면 <Link to="/login" state={{ from: `/posts/${id}` }}>로그인</Link>하세요.
          </p>
        )}
        {commentError && <p className="error">{commentError}</p>}
      </section>
    </section>
  );
}

export default PostDetail;
