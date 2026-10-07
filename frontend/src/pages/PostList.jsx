import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { api, formatDate } from "../api";
import { useAuth } from "../auth";

const PAGE_SIZE = 10; // 백엔드 PostService.PAGE_SIZE와 같아야 함

function PostList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Math.max(Number(searchParams.get("page")) || 0, 0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    api(`/api/posts?page=${page}`)
      .then((res) => {
        setData(res);
        setError("");
      })
      .catch((e) => setError(e.message));
  }, [page]);

  const goWrite = () => {
    navigate(user ? "/write" : "/login", { state: { from: "/write" } });
  };

  return (
    <section>
      <h1 className="page-title">게시판</h1>

      {error && <p className="error">{error}</p>}

      <table className="board">
        <thead>
          <tr>
            <th className="col-no">번호</th>
            <th className="col-title">제목</th>
            <th className="col-author">작성자</th>
            <th className="col-date">작성일</th>
          </tr>
        </thead>
        <tbody>
          {data?.posts.map((post, i) => (
            <tr key={post.id}>
              {/* DB id 대신 전체 글 수 기준으로 번호를 매겨 항상 1부터 빈틈없이 표시 */}
              <td className="col-no">{data.totalElements - data.page * PAGE_SIZE - i}</td>
              <td className="col-title">
                <Link to={`/posts/${post.id}`}>{post.title}</Link>
                {post.commentCount > 0 && <span className="comment-count">[{post.commentCount}]</span>}
              </td>
              <td className="col-author">{post.author}</td>
              <td className="col-date">{formatDate(post.createdAt)}</td>
            </tr>
          ))}
          {data && data.posts.length === 0 && (
            <tr>
              <td colSpan={4} className="empty">아직 작성된 글이 없습니다. 첫 글을 남겨보세요!</td>
            </tr>
          )}
          {!data && !error && (
            <tr>
              <td colSpan={4} className="empty">불러오는 중...</td>
            </tr>
          )}
        </tbody>
      </table>

      {data && data.totalPages > 1 && (
        <div className="pagination">
          {Array.from({ length: data.totalPages }, (_, i) => (
            <button
              key={i}
              className={`page-btn ${i === data.page ? "active" : ""}`}
              onClick={() => setSearchParams(i === 0 ? {} : { page: String(i) })}
            >
              {i + 1}
            </button>
          ))}
        </div>
      )}

      <div className="actions">
        <button className="btn btn-primary" onClick={goWrite}>글쓰기</button>
      </div>
    </section>
  );
}

export default PostList;
