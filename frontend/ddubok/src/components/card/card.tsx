`use client`;

import Image from "next/image";
import { useEffect, useState } from "react";

import { IDetailCardDto } from "@interface/components/card";

import "@styles/scrollHide.css";
import "@styles/rotateCard.css";
import Button from "@components/button/button";

const Card = ({ width, height, path, content, state, effect, flip }: IDetailCardDto) => {
	const [isFlipped, setIsFlipped] = useState(false);
	const [tempState, setTempState] = useState(state);

	const effectClasses = () => {
		switch (effect) {
			case 1:
				return "bg-yellow-500";
			case 2:
				return "bg-red-500";
			default:
				return "bg-transparent";
		}
	};

	const toggleFlip = () => {
		if (flip) {
			setIsFlipped(!isFlipped);
		}
	};

	useEffect(() => {
		setIsFlipped(false);
	}, [path]);

	const clickUnlockContent = () => {
		setTempState("OPEN");
	};

	return (
		<div
			className="perspective"
			style={{ width: `${width}px`, height: `${height}px` }}
			onClick={toggleFlip}
		>
			<div
				className={`relative preserve-3d transition-transform duration-700 ${isFlipped ? "rotate-y-180" : ""}`}
				style={{ width: `${width}px`, height: `${height}px` }}
			>
				{/* 카드 앞면 */}
				<div
					className={`absolute w-full h-full backface-hidden rounded-lg shadow-lg overflow-hidden ${effectClasses()}`}
				>
					{path ? (
						<Image
							src={path}
							alt="Card"
							fill
							sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
							style={{ objectFit: "cover" }}
							quality={80}
							priority
							unoptimized
						/>
					) : (
						<div className="font-nexonBold text-xl flex justify-center items-center h-full bg-white">
							직접 만들기
						</div>
					)}
				</div>

				{/* 카드 뒷면 */}
				<div
					className="absolute w-full h-full backface-hidden rotate-y-180 flex items-center justify-center rounded-lg shadow-lg overflow-hidden text-justify"
					style={{ backgroundColor: "#f0f0f0" }}
				>
					{state === "OPEN" ? (
						<div
							className="font-nexonRegular my-10 px-8 leading-tight text-sm overflow-hidden overflow-y-scroll scrollbar-hide"
							style={{ width: `${width}px`, height: `calc(${height}px - 80px)` }}
						>
							{content}
						</div>
					) : state === "READY" ? (
						<div className="font-nexonRegular text-lg flex justify-center items-center h-full ">
							편지는 11월 13일 오후 6시부터 <br />
							확인할 수 있습니다.
						</div>
					) : (
						<div
							className="font-nexonRegular text-lg flex justify-center items-center h-full"
							style={{ width: `${width}px`, height: `calc(${height}px - 80px)` }}
						>
							<p className="mb-4">
								편지에 부적절한 내용이 <br />
								포함되어 있습니다.
							</p>
							<Button
								text="편지 내용<br />확인하기"
								color="gradient"
								size="short"
								font="both"
								shadow="gradient"
								onClick={clickUnlockContent}
							/>
						</div>
					)}
					<Image
						src="/assets/fortune-reverse.png"
						alt="운세 카드"
						fill
						className="absolute z-[-1]"
						unoptimized
					/>
				</div>
			</div>
		</div>
	);
};

export default Card;
