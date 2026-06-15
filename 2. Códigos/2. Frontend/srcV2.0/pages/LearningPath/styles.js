import styled from "styled-components";

export const Page = styled.main`
    min-height: 100vh;
    background:
            radial-gradient(circle at 20% 15%, rgba(236, 172, 12, 0.18), transparent 24%),
            radial-gradient(circle at 85% 35%, rgba(227, 92, 51, 0.18), transparent 26%),
            radial-gradient(circle at 18% 80%, rgba(40, 128, 120, 0.24), transparent 28%),
            linear-gradient(180deg, var(--dark) 0%, var(--green) 100%);
    padding-bottom: 80px;
`;

export const Hero = styled.section`
  padding: 70px 120px 40px;
  color: var(--cream);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;

  > div:first-child {
    max-width: 680px;
  }

  span {
    color: var(--yellow);
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 1px;
  }

  h1 {
    font-size: 54px;
    margin: 14px 0;
    line-height: 1.05;
  }

  p {
    max-width: 650px;
    color: rgba(241, 240, 226, 0.82);
    line-height: 1.7;
    font-size: 17px;
  }
`;

export const UserInfo = styled.div`
  background: rgba(241, 240, 226, 0.1);
  border: 1px solid rgba(241, 240, 226, 0.18);
  border-radius: 28px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  backdrop-filter: blur(10px);
`;

export const Avatar = styled.div`
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--yellow);
  color: var(--dark);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 22px;
  border: 5px solid var(--cream);
`;

export const Stats = styled.div`
  display: flex;
  gap: 12px;
`;

export const StatCard = styled.div`
  min-width: 82px;
  background: var(--cream);
  color: var(--dark);
  border-radius: 18px;
  padding: 14px;
  text-align: center;

  strong {
    display: block;
    font-size: 24px;
    color: var(--orange);
  }

  span {
    font-size: 12px;
    color: var(--brown-dark);
    font-weight: 700;
    text-transform: none;
    letter-spacing: 0;
  }
`;

export const MapWrapper = styled.section`
    max-width: 720px;
    margin: 30px auto 0;
    background:
            radial-gradient(circle at 10% 20%, rgba(236, 172, 12, 0.18), transparent 20%),
            radial-gradient(circle at 90% 60%, rgba(227, 92, 51, 0.16), transparent 22%),
            var(--cream);
    border-radius: 42px;
    padding: 70px 32px;
    box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
    border: 5px solid var(--yellow);
    position: relative;

    &::before {
        content: "";
        position: absolute;
        inset: 22px;
        border: 3px dashed rgba(227, 92, 51, 0.45);
        border-radius: 30px;
        pointer-events: none;
    }
`;

export const Path = styled.div`
  width: 100%;
  position: relative;
  z-index: 2;
`;

export const PhaseCard = styled.div`
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24px;
    margin: 0 auto;

    &.right,
    &.left {
        justify-content: center;
        padding: 0;
        flex-direction: row;
    }

    &.locked {
        opacity: 0.65;
    }
`;

export const MissionButton = styled.button`
    width: 112px;
    height: 112px;
    min-width: 112px;
    border-radius: 50%;
    border: 8px solid var(--cream);
    background: linear-gradient(145deg, var(--yellow), var(--orange));
    color: var(--dark);
    font-size: 34px;
    font-weight: 900;
    box-shadow:
            0 10px 0 var(--brown-dark),
            0 20px 28px rgba(4, 4, 35, 0.28);
    cursor: pointer;
    transition: 0.3s;

    &:hover {
        transform: translateY(-7px) scale(1.05);
    }

    &.locked {
        background: linear-gradient(145deg, var(--brown-light), var(--brown-dark));
        color: var(--cream);
        cursor: not-allowed;

        &:hover {
            transform: none;
        }
    }

    &.boss {
        background: linear-gradient(145deg, var(--orange), var(--dark));
        color: var(--cream);
        box-shadow:
                0 10px 0 var(--brown-dark),
                0 20px 28px rgba(227, 92, 51, 0.35);
    }
`;

export const Connector = styled.div`
    width: 10px;
    height: 82px;
    margin: 12px auto;
    background: repeating-linear-gradient(
            180deg,
            var(--yellow) 0px,
            var(--yellow) 12px,
            var(--orange) 12px,
            var(--orange) 24px
    );
    border-radius: 30px;
    box-shadow: 0 0 18px rgba(236, 172, 12, 0.5);
`;

export const Content = styled.div`
    width: 330px;
    background: var(--cream);
    padding: 24px;
    border-radius: 26px;
    box-shadow: 0 14px 35px rgba(4, 4, 35, 0.18);
    border: 3px solid var(--yellow);
    position: relative;

    &::before {
        content: "XP";
        position: absolute;
        top: -15px;
        right: 20px;
        background: var(--orange);
        color: var(--cream);
        padding: 7px 12px;
        border-radius: 999px;
        font-size: 11px;
        font-weight: 900;
    }

    h2 {
        color: var(--orange);
        font-size: 16px;
        margin-top: 10px;
    }

    h3 {
        color: var(--dark);
        font-size: 22px;
        margin: 4px 0 14px;
        line-height: 1.2;
    }
`;

export const Badge = styled.span`
  display: inline-block;
  background: rgba(40, 128, 120, 0.12);
  color: var(--green);
  padding: 7px 14px;
  border-radius: 30px;
  font-size: 12px;
  font-weight: 800;
`;

export const Topics = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 7px;
  margin: 12px 0 20px;

  li {
    color: var(--brown-dark);
    font-size: 14px;
    line-height: 1.4;

    &::before {
      content: "• ";
      color: var(--orange);
      font-weight: 900;
    }
  }
`;

export const ActionButton = styled.button`
  width: 100%;
  background: var(--green);
  color: var(--white);
  border: none;
  padding: 13px;
  border-radius: 16px;
  font-weight: 800;
  cursor: pointer;
  transition: 0.3s;

  &:hover:not(:disabled) {
    background: var(--dark);
  }

  &:disabled {
    background: var(--brown-light);
    cursor: not-allowed;
  }
`;