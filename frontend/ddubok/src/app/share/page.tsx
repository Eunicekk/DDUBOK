"use client";

import { useRouter } from "next/navigation";
import { useContext, useEffect, useMemo, useState } from "react";

import Button from "@components/button/button";
import Loading from "@components/common/loading";
import Card from "@components/card/card";
import Modal from "@components/common/modal";
import { ModalContext } from "@context/modal-context";
import useAuthToken from "@lib/utils/tokenUtils";
import { selectPreviewList } from "@lib/api/card-load-api";
import { getTokenInfo } from "@lib/utils/authUtils";

import Slider from "react-slick";

const Request = () => {
	const router = useRouter();
	const { isModalOpen, openModal, closeModal } = useContext(ModalContext);
	const { accessToken } = useAuthToken();
	const [isLoading, setIsLoading] = useState(true);
	const [nickname, setNickname] = useState("");
	const [imageArray, setImageArray] = useState<string[]>([]);

	const settings = {
		infinite: true,
		speed: 500,
		slidesToShow: 1,
		slidesToScroll: 1,
		centerMode: true,
		variableWidth: true,
		arrows: false,
		autoplay: true,
		autoplaySpeed: 4000,
		adaptiveHeight: true,
	};

	useEffect(() => {
		const loadPriveiwImages = async () => {
			if (accessToken) {
				try {
					const decodedToken = getTokenInfo(accessToken);
					const response = await selectPreviewList(decodedToken.memberId);
					console.log(response.data.data);
					setNickname(response.data.data.nickname);
					setImageArray(response.data.data.cardUrl);
					setIsLoading(false);
				} catch (error) {
					console.error("Error fetching card images:", error);
					router.push("/login");
				}
			}
		};

		loadPriveiwImages();
	}, [accessToken]);

	const cardImages = useMemo(() => {
		return imageArray.length >= 3
			? imageArray.map((image) => ({ image, effect: 0 }))
			: [
					{ image: "/assets/template/kde-card-2.jpg", effect: 0 },
					{ image: "/assets/template/kkm-card.png", effect: 0 },
					{ image: "/assets/template/kde-card.jpg", effect: 0 },
					{ image: "/assets/template/psh-card.jpg", effect: 0 },
			  ];
	}, [imageArray]);

	return (
		<div id="request">
			{isLoading ? (
				<div className="flex w-full h-screen items-center justify-center">
					<Loading />
				</div>
			) : (
				<>
					<div className="text-white font-nexonRegular flex flex-col items-center pt-8 text-center">
						<p className="mb-4 text-lg leading-normal">
							수능을 앞둔 <br />
							<span className="font-nexonBold">{nickname}</span> 님을 위해 <br />
							행운카드을 만들어주세요🍀
						</p>
						<p className="text-sm">
							현재까지 <span className="font-nexonBold">{imageArray.length}</span>개의 행운카드를
							받았어요💌
						</p>
					</div>
					<div className="w-full max-w-[480px] mx-auto mt-8">
						<Slider {...settings}>
							{cardImages.map((card, index) => (
								<Card
									key={index}
									width={250}
									height={445}
									path={card.image}
									effect={card.effect}
								/>
							))}
						</Slider>
					</div>
					<div className="flex justify-center gap-2 py-12">
						<Button
							text="나의 카드북<br/>만들기"
							color="purple"
							size="short"
							font="both"
							shadow="purple"
							onClick={() => openModal()}
						/>
						<Button
							text="행운카드<br/>만들어주기"
							color="green"
							size="short"
							font="both"
							shadow="green"
							onClick={() => {
								router.push("/create");
							}}
						/>
					</div>
				</>
			)}
			<div
				className={`transition-opacity duration-300
					${isModalOpen ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"}`}
			>
				<Modal>
					<p className="text-sm text-center mb-6">
						로그인이 필요한 서비스입니다. <br />
						로그인하시겠습니까?
					</p>
					<div className="flex justify-evenly">
						<Button
							text="아니오"
							color="gray"
							size="small"
							font="small"
							shadow="gray"
							onClick={() => closeModal()}
						/>
						<Button
							text="예"
							color="green"
							size="small"
							font="small"
							shadow="green"
							onClick={() => {
								router.push("/login");
							}}
						/>
					</div>
				</Modal>
			</div>
		</div>
	);
};

export default Request;
